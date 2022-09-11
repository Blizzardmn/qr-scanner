package com.tools.easy.scanner.advertise.base

import android.view.View
import android.widget.TextView
import com.tools.easy.scanner.advertise.conf.ConfigId

/**
 *  description :
 */
abstract class BaseNative(adPos: AdPos, configId: ConfigId): BaseAd(adPos, configId) {

    abstract fun showTitle(parentV: View?, textView: TextView)
    abstract fun showBody(parentV: View?, textView: TextView)
    abstract fun showImage(parentV: View?, mediaView: View)
    abstract fun showIcon(parentV: View?, mediaView: View)
    abstract fun showCta(parentV: View?, view: View)
    abstract fun register(parentV: View)

}