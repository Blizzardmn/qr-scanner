package com.tools.easy.scanner.advertise.base

import com.tools.easy.scanner.advertise.conf.ConfigId

/**
 *  description :
 */
abstract class BaseAd(val adPos: AdPos, val configId: ConfigId) {

    protected var actShown: (() -> Unit)? = null
    protected var actClick: (() -> Unit)? = null
    protected var actDismiss: (() -> Unit)? = null

    abstract fun buildInAd(ad: Any)

    fun defineListener(adsListener: AdsListener) {
        actShown = { adsListener.onAdShown() }
        actClick = { adsListener.onAdClick() }
        actDismiss = { adsListener.onAdDismiss() }
    }

    open fun onDestroy() {

    }
}