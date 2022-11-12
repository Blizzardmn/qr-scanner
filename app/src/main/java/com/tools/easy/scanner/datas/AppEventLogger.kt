package com.tools.easy.scanner.datas

import com.google.firebase.analytics.FirebaseAnalytics
import com.tools.easy.scanner.App

/**
 *  description :
 */
class AppEventLogger {

    private var firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(App.ins)
    companion object {
        val ins: AppEventLogger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            AppEventLogger()
        }
    }

    fun logEvent(action: String) {
        firebaseAnalytics.logEvent(action, null)
    }
}