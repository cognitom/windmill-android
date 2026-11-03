package io.github.cognitom.windmill

import android.content.ComponentName
import android.content.Context
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager

/** 切り替え先の IME の ID を、サービスと設定画面で同じ規則で決めるためのもの */
object Imes {
    const val GBOARD_PACKAGE = "com.google.android.inputmethod.latin"

    private const val PREFS = "settings"
    private const val KEY_JAPANESE_IME_ID = "japanese_ime_id"

    /** IME の ID は ComponentName の短縮形なので、InputMethodInfo を引かずに組み立てられる */
    fun englishId(context: Context): String =
        ComponentName(context, EnglishImeService::class.java).flattenToShortString()

    /** 選んだものが無ければ Gboard を既定にする。Gboard も無ければ null */
    fun japaneseId(context: Context): String? =
        prefs(context).getString(KEY_JAPANESE_IME_ID, null) ?: defaultJapaneseId(context)

    fun setJapaneseId(context: Context, id: String) {
        prefs(context).edit().putString(KEY_JAPANESE_IME_ID, id).apply()
    }

    /** 自分以外の、入っている IME すべて。有効でないものも選べるように一覧には入れる */
    fun otherImes(context: Context): List<InputMethodInfo> {
        val englishId = englishId(context)
        return imm(context).inputMethodList.filter { it.id != englishId }
    }

    fun isEnabled(context: Context, id: String): Boolean =
        imm(context).enabledInputMethodList.any { it.id == id }

    private fun defaultJapaneseId(context: Context): String? =
        imm(context).inputMethodList.firstOrNull { it.packageName == GBOARD_PACKAGE }?.id

    private fun imm(context: Context) =
        context.getSystemService(InputMethodManager::class.java)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
