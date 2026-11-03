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

`output/`、`.gradle/`、`build/`、`local.properties` は生成物なのでコミットしない。

## ビルド

`gradle` は `compose.agent.yaml` の `gradle` サービスの中で動く。polar の shim がホストからそこへ届ける。

```bash
bash scripts/build.sh   # output/windmill-android-debug.apk ができる
```

Gradle のキャッシュとデバッグ用の署名鍵は、名前付きボリューム `windmill-android-gradle-cache` に置く。環境を作り直しても同じ鍵で署名されるので、実機へ上書きインストールできる。

## 守ること

- JDK、Android SDK、adb をホストに入れない。実機へは APK ファイルで渡す
- Gradle Wrapper (`gradlew`) は置かない。Gradle はイメージに入っているものを使う
- build-tools のバージョンを変えるときは、`Dockerfile` の `BUILD_TOOLS_VERSION` と `app/build.gradle.kts` の `buildToolsVersion` を揃える。compileSdk を上げるときは `ANDROID_PLATFORM` も揃える
- コードを触ったら `scripts/build.sh` を通す
- コメントとコミットメッセージは日本語。コメントには「なぜそうしたか」を書く
