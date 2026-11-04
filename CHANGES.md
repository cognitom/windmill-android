# 変更履歴

各バージョンの中身は、そのバージョンに入ったPRのリンクを並べただけ。
リリースのたびに `scripts/release.sh` が先頭へ書き足すので、手では編集しない。
何を直したかは、PRとそこから辿れるissueを参照。

## v0.1.0 (2026-11-04)

- Android のビルド環境をコンテナで用意する ([#3](https://github.com/cognitom/windmill-android/pull/3))
- rootful の docker で shim が root 以外で入っても Gradle が動くようにする ([#4](https://github.com/cognitom/windmill-android/pull/4))
- 英語用の IME と、変換・無変換で IME を切り替えるユーザー補助サービスを入れる ([#5](https://github.com/cognitom/windmill-android/pull/5))
- アイコンと表示名を整え、README に導入手順を書く ([#9](https://github.com/cognitom/windmill-android/pull/9))
- 物理キーボードが無いときは、英語用の IME から日本語側の IME へ戻す ([#10](https://github.com/cognitom/windmill-android/pull/10))
- README の冒頭になぜ必要かを書き、表示名を「Windmill 言語切り替え」にそろえる ([#11](https://github.com/cognitom/windmill-android/pull/11))
- GitHub Actions で release の APK をビルドしてリリースに貼る ([#13](https://github.com/cognitom/windmill-android/pull/13))

---

