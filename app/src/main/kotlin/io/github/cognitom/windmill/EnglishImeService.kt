package io.github.cognitom.windmill

import android.inputmethodservice.InputMethodService

/**
 * 英語用の IME。物理キーボードで打つ前提なので画面キーボードは持たず、キーも処理しない。
 * InputMethodService の既定の onKeyDown は戻るキー以外を false で返すので、
 * キーはそのままアプリへ届く。
 */
class EnglishImeService : InputMethodService() {
    // 物理キーボードを外したときでも、空の入力ビューで画面の下が塞がらないようにする。
    // super は @CallSuper なので呼ぶが、結果は使わない
    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return false
    }

    override fun onEvaluateFullscreenMode(): Boolean = false
}
