package com.tools.easy.scanner.advertise.base

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.tools.easy.scanner.advertise.conf.ConfigId

/**
 *  description :
 */
class AdmobNative(adPos: AdPos, configId: ConfigId): BaseNative(adPos, configId) {

    private var mNative: NativeAd? = null
    var actLoadAdError: ((error: String) -> Unit)? = null

    val adListener = object : AdListener() {
        override fun onAdImpression() {
            actShown?.invoke()
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            actLoadAdError?.invoke(p0.message)
        }

        override fun onAdClicked() {
            actClick?.invoke()
        }

        override fun onAdClosed() {
            actDismiss?.invoke()
        }
    }

    override fun onDestroy() {
        mNative?.destroy()
    }

    override fun showTitle(parentV: View?, textView: TextView) {
        textView.text = mNative?.headline
        textView.visibility = View.VISIBLE
        if (parentV is NativeAdView) {
            parentV.headlineView = textView
        }
    }

    override fun showBody(parentV: View?, textView: TextView) {
        textView.text = mNative?.body
        textView.visibility = View.VISIBLE
        if (parentV is NativeAdView) {
            parentV.bodyView = textView
        }
    }

    override fun showImage(parentV: View?, mediaView: View) {
        if (mediaView !is MediaView) return
        mediaView.visibility = View.VISIBLE
        mNative?.mediaContent?.apply {
            mediaView.setMediaContent(this)
        }
        if (parentV !is NativeAdView) return
        parentV.mediaView = mediaView
    }

    override fun showIcon(parentV: View?, mediaView: View) {
        if (mediaView !is ImageView) return
        mediaView.setImageDrawable(mNative?.icon?.drawable)
        mediaView.visibility = View.VISIBLE
        if (parentV !is NativeAdView) return
        parentV.iconView = mediaView
    }

    override fun showCta(parentV: View?, view: View) {
        if (view is TextView) {
            view.text = mNative?.callToAction
        }
        view.visibility = View.VISIBLE
        if (parentV !is NativeAdView) return
        parentV.callToActionView = view
    }

    override fun register(parentV: View) {
        if (parentV is NativeAdView) {
            mNative?.apply {
                parentV.setNativeAd(this)
            }
        }
    }

    override fun buildInAd(ad: Any) {
        mNative = ad as? NativeAd
    }

}