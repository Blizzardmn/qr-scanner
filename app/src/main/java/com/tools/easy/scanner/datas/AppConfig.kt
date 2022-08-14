package com.tools.easy.scanner.datas

import android.content.Context
import com.tools.easy.scanner.App

/**
 *  description :
 */
class AppConfig {

    companion object {
        val ins: AppConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppConfig() }
    }

    private val sp = App.ins.getSharedPreferences("qr_own_self", Context.MODE_PRIVATE)


    var isScanBeepOn: Boolean
        get() {
            return sp.getBoolean("is_beep_on", false)
        }
        set(value) {
            sp.edit().putBoolean("is_beep_on", value).apply()
        }

    var isVibrateOn: Boolean
        get() {
            return sp.getBoolean("is_vibrate_on", false)
        }
        set(value) {
            sp.edit().putBoolean("is_vibrate_on", value).apply()
        }
}