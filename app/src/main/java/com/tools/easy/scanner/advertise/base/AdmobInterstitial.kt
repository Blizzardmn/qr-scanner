package com.tools.easy.scanner.advertise.base

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.tools.easy.scanner.advertise.conf.ConfigId

/**
 *  description :
 */
class AdmobInterstitial(adPos: AdPos, configId: ConfigId): BaseInterstitial(adPos, configId) {

    private var mInterstitial: InterstitialAd? = null
    private val showListener = object : FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            actShown?.invoke()
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            actDismiss?.invoke()
        }

        override fun onAdDismissedFullScreenContent() {
            actDismiss?.invoke()
        }

        override fun onAdClicked() {
            actClick?.invoke()
        }
    }

    override fun show(activity: Activity): Boolean {
        if (mInterstitial == null) return false
        mInterstitial?.fullScreenContentCallback = showListener
        mInterstitial?.show(activity)
        return true
    }

    override fun buildInAd(ad: Any) {
        mInterstitial = ad as? InterstitialAd
    }
}