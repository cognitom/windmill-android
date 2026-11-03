package io.github.cognitom.windmill

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

/**
 * 英語用の IME。物理キーボードで打つ前提なので画面キーボードは持たず、キーも処理しない。
 * InputMethodService の既定の onKeyDown は戻るキー以外を false で返すので、
 * キーはそのままアプリへ届く。
 *
 * 画面キーボードが無いので、物理キーボードが無い状態でこの IME が選ばれていると
 * ほかの IME へ戻る手段がなくなる。そのため物理キーボードが無いときは日本語側の IME へ戻す。
 */
class EnglishImeService : InputMethodService() {
    // 切り替えに失敗するたびに選択画面を出すと、入力欄にフォーカスが来るたびに出続けてしまう。
    // 物理キーボードがつながるまでは1回だけにする
    private var pickerShown = false

    // 物理キーボードを外したときでも、空の入力ビューで画面の下が塞がらないようにする。
    // super は @CallSuper なので呼ぶが、結果は使わない
    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return false
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        leaveIfNoKeyboard(resources.configuration)
    }

    // 英語用の IME を使っている最中にキーボードを外したときは、入力欄のフォーカスが変わらないので
    // onStartInput が呼ばれない。構成の変化で気づく
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        leaveIfNoKeyboard(newConfig)
    }

    private fun leaveIfNoKeyboard(config: Configuration) {
        if (hasKeyboard(config)) {
            pickerShown = false
            return
        }
        if (switchToJapanese()) return
        if (!pickerShown) {
            pickerShown = true
            getSystemService(InputMethodManager::class.java).showInputMethodPicker()
        }
    }

    /** 切り替えられたら true。切り替え先が決まらない・有効でない・失敗したときは false */
    private fun switchToJapanese(): Boolean {
        val target = Imes.japaneseId(this)
        if (target == null) {
            Log.w(TAG, "日本語側の IME が決まっていない")
            return false
        }
        // 有効でない IME へ切り替えようとすると例外になるので、先に確かめる
        if (!Imes.isEnabled(this, target)) {
            Log.w(TAG, "日本語側の IME が有効になっていない: $target")
            return false
        }
        return try {
            switchInputMethod(target)
            true
        } catch (e: RuntimeException) {
            // 確かめた直後に無効にされた場合などに備える。IME が使えなくなるよりは選択画面を出したい
            Log.w(TAG, "IME を切り替えられなかった: $target", e)
            false
        }
    }

    private companion object {
        const val TAG = "EnglishImeService"

        /**
         * キーの無い構成か、キーボードが隠されている (折りたたみ式などで閉じている) 構成なら、
         * 物理キーボードでは打てないとみなす
         */
        fun hasKeyboard(config: Configuration): Boolean =
            config.keyboard != Configuration.KEYBOARD_NOKEYS &&
                config.hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES
    }
}
