package io.github.cognitom.windmill

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.SoftKeyboardController
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * 変換で日本語の IME へ、無変換で英語用の IME へ切り替えるユーザー補助サービス。
 * IME を直接指定して切り替えられるのは、アプリからは SoftKeyboardController.switchToInputMethod()
 * (API 30) だけなので、ユーザー補助サービスにしている。
 */
class KeySwitchService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Android 13 以降は、同じパッケージの IME ならユーザー補助サービスから有効にできる。
        // 利用者が IME の設定まで行かずに済むように、起動のたびに有効にしておく
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = softKeyboardController.setInputMethodEnabled(Imes.englishId(this), true)
            if (status != SoftKeyboardController.ENABLE_IME_SUCCESS) {
                Log.w(TAG, "英語用の IME を有効にできなかった: status=$status")
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val target = when (event.keyCode) {
            KeyEvent.KEYCODE_HENKAN -> Imes.japaneseId(this)
            KeyEvent.KEYCODE_MUHENKAN -> Imes.englishId(this)
            else -> return false
        }
        // 押したときに1回だけ切り替える。押しっぱなしのリピートや離したときは何もしない
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            if (target == null) {
                Log.w(TAG, "日本語側の IME が決まっていない")
            } else if (!softKeyboardController.switchToInputMethod(target)) {
                // 有効になっていない IME へは切り替えられない
                Log.w(TAG, "IME を切り替えられなかった: $target")
            }
        }
        // 切り替え専用のキーなので、押下も離しもアプリや IME へは渡さない
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    private companion object {
        const val TAG = "KeySwitchService"
    }
}
