package com.tools.easy.scanner.advertise.base

/**
 *  description :
 */
open class AdsListener {

    open fun onAdLoaded(ad: BaseAd) {}

    open fun onAdError(err: String?) {}

    open fun onAdShown() {}

    open fun onAdClick() {}

    open fun onAdDismiss() {}

}