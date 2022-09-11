package com.tools.easy.scanner.advertise

import android.content.Context
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.tools.easy.scanner.advertise.base.*
import com.tools.easy.scanner.advertise.conf.ConfigId

/**
 *  description :
 */
open class AdmobLoader {

    fun loadOpen(ctx: Context, adPos: AdPos, configId: ConfigId, load: (ad: BaseAd?) -> Unit ) {
        val admobOpen = AdmobOpen(adPos, configId)
        val loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                load.invoke(null)
            }

            override fun onAdLoaded(p0: AppOpenAd) {
                admobOpen.buildInAd(p0)
                load.invoke(admobOpen)
            }
        }
        AppOpenAd.load(
            ctx.applicationContext,
            configId.id,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            loadCallback
        )
    }

    fun loadInterstitial(ctx: Context, adPos: AdPos, configId: ConfigId, load: (ad: BaseAd?) -> Unit ) {
        val admobInterstitial = AdmobInterstitial(adPos, configId)
        InterstitialAd.load(ctx, configId.id, AdRequest.Builder().build(), object :
            InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                admobInterstitial.buildInAd(p0)
                load.invoke(admobInterstitial)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                load.invoke(null)
            }
        })
    }

    fun loadNative(ctx: Context, adPos: AdPos, configId: ConfigId, load: (ad: BaseAd?) -> Unit ) {
        val admobNative = AdmobNative(adPos, configId)
        admobNative.actLoadAdError = {
            load.invoke(null)
        }
        AdLoader.Builder(ctx, configId.id)
            .forNativeAd {
                admobNative.buildInAd(it)
                load.invoke(admobNative)
            }
            .withAdListener(admobNative.adListener)
            .withNativeAdOptions(NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

}