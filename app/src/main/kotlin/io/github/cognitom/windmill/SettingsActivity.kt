package io.github.cognitom.windmill

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

/**
 * サービスと IME の有効化への導線と、日本語側の IME の選択だけを置く画面。
 * 依存を増やさないために AndroidX は使わず、フレームワークの Activity で作る。
 */
class SettingsActivity : Activity() {

    private lateinit var serviceStatus: TextView
    private lateinit var imeStatus: TextView
    private lateinit var japaneseImes: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // targetSdk 35 では Android 15 以降で全画面表示が強制されるので、システムバーの分を空ける。
        // 14 以前はシステムがすでに空けているので、重ねて空けないようにする
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            findViewById<View>(R.id.root).setOnApplyWindowInsetsListener { v, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
        }

        serviceStatus = findViewById(R.id.service_status)
        imeStatus = findViewById(R.id.ime_status)
        japaneseImes = findViewById(R.id.japanese_imes)

        findViewById<Button>(R.id.open_accessibility_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.open_ime_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        // Android 12 以前は自動で有効にできないので、注記を出さない
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            findViewById<View>(R.id.ime_note).visibility = View.GONE
        }
    }

    // ほかの設定画面から戻ってきたときに状態が変わっているので、表示のたびに作り直す
    override fun onResume() {
        super.onResume()
        serviceStatus.text = statusText(isServiceEnabled())
        imeStatus.text = statusText(Imes.isEnabled(this, Imes.englishId(this)))
        renderJapaneseImes()
    }

    private fun renderJapaneseImes() {
        japaneseImes.setOnCheckedChangeListener(null)
        japaneseImes.removeAllViews()

        val imes = Imes.otherImes(this)
        if (imes.isEmpty()) {
            japaneseImes.addView(TextView(this).apply { setText(R.string.no_ime_found) })
            return
        }

        val selected = Imes.japaneseId(this)
        val ids = mutableMapOf<Int, String>()
        for (ime in imes) {
            val button = RadioButton(this).apply {
                id = View.generateViewId()
                val label = ime.loadLabel(packageManager)
                text = if (Imes.isEnabled(context, ime.id)) label
                else "$label ${getString(R.string.ime_disabled_suffix)}"
            }
            ids[button.id] = ime.id
            japaneseImes.addView(button)
            if (ime.id == selected) japaneseImes.check(button.id)
        }
        japaneseImes.setOnCheckedChangeListener { _, checkedId ->
            ids[checkedId]?.let { Imes.setJapaneseId(this, it) }
        }
    }

    private fun isServiceEnabled(): Boolean {
        val self = ComponentName(this, KeySwitchService::class.java)
        return getSystemService(AccessibilityManager::class.java)
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { ComponentName.unflattenFromString(it.id) == self }
    }

    private fun statusText(enabled: Boolean): String =
        getString(if (enabled) R.string.status_enabled else R.string.status_disabled)
}
