package com.tools.easy.scanner.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.tools.easy.scanner.App
import com.tools.easy.scanner.advertise.AdConst
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.advertise.base.AdsListener
import com.tools.easy.scanner.advertise.base.BaseAd
import com.tools.easy.scanner.advertise.base.BaseInterstitial
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityOpenBinding
import com.tools.easy.scanner.ui.new.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 *  description :
 */
class OpenActivity: BasicActivity<ActivityOpenBinding>() {

    override fun vBinding(): ActivityOpenBinding {
        return ActivityOpenBinding.inflate(layoutInflater)
    }

    companion object {
        fun restart(activity: Activity) {
            val intent = Intent(activity, OpenActivity::class.java)
            intent.putExtra("restart", true)
            activity.startActivity(intent)
        }
    }

    private var isAdShown = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startProgress(10_000L) {
            if (isActivityPaused()) {
                return@startProgress
            }
            if (isAdShown) return@startProgress
            openMain()
        }

        isAdShown = false
        lifecycleScope.launch {
            AdLoader.preloadAd(AdConst.adMain)
            AdLoader.preloadAd(AdConst.adIns)
            delay(600L)

            AdLoader.loadAd(App.ins, AdConst.adOpen, adListener)
        }
    }

    private val adListener = object : AdsListener() {
        override fun onAdLoaded(ad: BaseAd) {
            if (isActivityPaused()) {
                AdLoader.add2cache(AdConst.adOpen, ad)
                return
            }
            if (ad !is BaseInterstitial) {
                openMain()
                return
            }

            if (ad.show(this@OpenActivity)) {
                return
            }
            openMain()
        }

        override fun onAdError(err: String?) {
            openMain()
        }

        override fun onAdShown() {
            isAdShown = true
        }

        override fun onAdDismiss() {
            if (!App.ins.isAppForeground()) {
                finish()
                return
            }
            openMain()
        }
    }

    private val atomicStarted = AtomicBoolean(false)
    private fun openMain() {
        if (atomicStarted.getAndSet(true)) return
        if (!intent.getBooleanExtra("restart", false)) {
            startActivity(Intent(this@OpenActivity, HomeActivity::class.java))
        }
        finish()
    }

    private var valueAni: ValueAnimator? = null
    private fun startProgress(costLong: Long, actionDone: () -> Unit) {
        valueAni?.cancel()
        valueAni = ValueAnimator.ofInt(binding.progressBar.progress, 100)
        valueAni?.duration = costLong
        valueAni?.addUpdateListener {
            val t = it.animatedValue as? Int ?: return@addUpdateListener
            binding.progressBar.progress = t
        }
        valueAni?.addListener(object : Animator.AnimatorListener{
            private var isCancel = false
            override fun onAnimationStart(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
                if (!isCancel) actionDone.invoke()
            }

            override fun onAnimationCancel(p0: Animator?) {
                isCancel = true
            }

            override fun onAnimationRepeat(p0: Animator?) {}
        })
        valueAni?.start()
    }
}