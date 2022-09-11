package com.tools.easy.scanner.advertise.base

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.tools.easy.scanner.advertise.conf.ConfigId

/**
 *  description :
 */
class AdmobOpen(adPos: AdPos, configId: ConfigId): BaseInterstitial(adPos, configId) {

    private var appOpenAd: AppOpenAd? = null

    private val showListener = object : FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            actShown?.invoke()
        }

        override fun onAdDismissedFullScreenContent() {
            actDismiss?.invoke()
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            actDismiss?.invoke()
        }

        override fun onAdClicked() {
            actClick?.invoke()
        }
    }

    override fun show(activity: Activity): Boolean {
        if (appOpenAd == null) return false
        appOpenAd?.fullScreenContentCallback = showListener
        appOpenAd?.show(activity)
        return true
    }

    override fun buildInAd(ad: Any) {
        appOpenAd = ad as? AppOpenAd
    }
}