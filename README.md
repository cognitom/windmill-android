# windmill-android

[Windmill](https://github.com/cognitom/windmill) キーボードの Android 用コンパニオンアプリ。キーボードの専用キーで、日本語の IME と英語用の IME を直接切り替える。

- 英語用の IME: 画面キーボードを持たない IME
- ユーザー補助サービス: 専用キーを受けて IME を切り替える

| キー | 切り替え先 |
|--|--|
| 変換 (`KEYCODE_HENKAN`) | 日本語側の IME。既定は Gboard で、設定画面で変えられる |
| 無変換 (`KEYCODE_MUHENKAN`) | 英語用の IME (Windmill English) |

## 使い方

1. APK を入れて、アプリ「Windmill」を開く
2. 「ユーザー補助の設定を開く」から「Windmill キー切り替え」を有効にする。APK から入れた場合、Android 13 以降は先にアプリ情報のメニューで「制限付き設定を許可」が要ることがある
3. 英語用の IME を有効にする。Android 13 以降はサービスが動きだしたときに自動で有効になる。Android 12 以前は「キーボードの設定を開く」から有効にする
4. 日本語側の IME を Gboard 以外にしたいときは、設定画面で選ぶ
