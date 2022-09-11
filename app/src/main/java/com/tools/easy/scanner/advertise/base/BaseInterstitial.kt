package com.tools.easy.scanner.advertise.base

import android.app.Activity
import com.tools.easy.scanner.advertise.conf.ConfigId

/**
 *  description :
 */
abstract class BaseInterstitial(adPos: AdPos, configId: ConfigId): BaseAd(adPos, configId) {

    abstract fun show(activity: Activity): Boolean
}