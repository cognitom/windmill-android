# CLAUDE.md

[Windmill](https://github.com/cognitom/windmill) キーボードの Android 用コンパニオンアプリ。英語用の IME とユーザー補助サービスを1つのアプリに入れる。対象は Android 11 (API 30) 以降。

## 構成

| パス | 中身 |
|--|--|
| `app/` | アプリ本体。モジュールは1つ |
| `settings.gradle.kts` `build.gradle.kts` | Gradle の設定。AGP と Kotlin のバージョンはルートの `build.gradle.kts` が正 |
| `Dockerfile` | JDK、Android SDK、Gradle を入れたイメージ。SDK と Gradle のバージョンは `ARG` が正 |
| `compose.agent.yaml` | Gradle を動かすコンテナ。`gradle` サービスが常駐する |
| `scripts/build.sh` | debug の APK を `output/` に出す |
| `scripts/build-release.sh` | 署名した release の APK を `output/` に出す |
| `scripts/release.sh` | `CHANGES.md` を書き足してタグを打つ |
| `scripts/bin/gradle` | ホストの `gradle` をコンテナへ届けるラッパー。shim の無い環境 (CI など) で PATH に足す |
| `.github/workflows/` | `test.yml` (PR と main への push) と `release.yml` (`v*` タグの push) |
| `CHANGES.md` | 変更履歴。`release.sh` が生成する |

`output/`、`.gradle/`、`build/`、`local.properties`、`keystore/` は生成物か秘密なのでコミットしない。

## ビルド

`gradle` は `compose.agent.yaml` の `gradle` サービスの中で動く。polar の shim がホストからそこへ届ける。shim が無ければ、ボリュームを作ってコンテナを立て、`scripts/bin` を PATH に足す。CI もそうしている。

```bash
docker volume create windmill-android-gradle-cache
docker compose -f compose.agent.yaml up -d --wait
export PATH="$PWD/scripts/bin:$PATH"
```

```bash
bash scripts/build.sh   # output/windmill-android-debug.apk ができる
gradle lintDebug        # lint。Test ワークフローも走らせる
```

Gradle のキャッシュとデバッグ用の署名鍵は、名前付きボリューム `windmill-android-gradle-cache` に置く。環境を作り直しても同じ鍵で署名されるので、実機へ上書きインストールできる。

## リリース

`main` で `scripts/release.sh` を実行する。使い方は cognitom/windmill と同じ。次のバージョン番号を決めて、`CHANGES.md` に前回のタグ以降にマージした PR を書き足してコミット・push し、タグを打って push する。

```bash
bash scripts/release.sh              # パッチ版を上げる (v0.1.0 → v0.1.1)。タグが無ければ v0.1.0
bash scripts/release.sh --minor      # マイナー版を上げる (v0.1.1 → v0.2.0)
bash scripts/release.sh v1.0.0       # バージョンを直接指定する
bash scripts/release.sh --dry-run    # CHANGES.md へ書く内容だけ見て終わる
```

タグを push すると Release ワークフロー (`.github/workflows/release.yml`) が `scripts/build-release.sh` で署名した APK を作り、GitHub のリリースに `windmill-android-vX.Y.Z.apk` として貼る。**`CHANGES.md` は手で書かない。** PR のタイトルがそのまま履歴の1行になる。

### バージョン

`versionName` と `versionCode` は Gradle のプロパティ (`-PversionName`、`-PversionCode`) で渡す。`build-release.sh` がタグ `vX.Y.Z` から作り、`versionCode` は `X * 10000 + Y * 100 + Z`。だからマイナー版とパッチ版は 99 まで。渡さないビルド (debug など) は `0.1.0` / `1` のまま。

### 署名

release の署名の値は環境変数で渡す。欠けていると release のビルドは止まる (未署名の APK を黙って作らせない)。

| 環境変数 | 中身 |
|--|--|
| `WINDMILL_KEYSTORE` | 鍵ストアのパス。**リポジトリのルートからの相対** (Gradle はコンテナの中で動くので、ホストの絶対パスは見えない)。置き場所は `.gitignore` 済みの `keystore/` |
| `WINDMILL_KEYSTORE_PASSWORD` | 鍵ストアのパスワード |
| `WINDMILL_KEY_ALIAS` | 鍵のエイリアス |
| `WINDMILL_KEY_PASSWORD` | 鍵のパスワード |

環境変数は、polar の shim なら `compose.agent.yaml` の `polar.env` ラベル、shim の無い環境なら `scripts/bin/gradle` がコンテナへ渡す。手元では shim を使う。

```bash
WINDMILL_KEYSTORE=keystore/windmill-release.jks \
WINDMILL_KEY_ALIAS=windmill \
WINDMILL_KEYSTORE_PASSWORD=... WINDMILL_KEY_PASSWORD=... \
  polar shim bash scripts/build-release.sh v0.1.0
```

CI では Secrets (`WINDMILL_KEYSTORE_BASE64` と上の3つ) から渡し、鍵ストアはビルドの後に消す。

署名の鍵は、のちに Google Play の Play App Signing に「アプリの署名鍵」として預ける前提で作ってある。GitHub のリリースと Play の版を同じ署名にするため。**鍵をなくすと、同じアプリとして更新できなくなる。** 鍵ストアとパスワードはリポジトリの外に控えておく。

## 守ること

- JDK、Android SDK、adb をホストに入れない。実機へは APK ファイルで渡す
- Gradle Wrapper (`gradlew`) は置かない。Gradle はイメージに入っているものを使う
- build-tools のバージョンを変えるときは、`Dockerfile` の `BUILD_TOOLS_VERSION` と `app/build.gradle.kts` の `buildToolsVersion` を揃える。compileSdk を上げるときは `ANDROID_PLATFORM` も揃える
- コードを触ったら `scripts/build.sh` を通す
- 鍵ストアとパスワードをリポジトリに置かない
- コメントとコミットメッセージは日本語。コメントには「なぜそうしたか」を書く
