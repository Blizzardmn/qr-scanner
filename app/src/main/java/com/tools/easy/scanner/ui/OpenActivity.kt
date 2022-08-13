package com.tools.easy.scanner.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import com.tools.easy.scanner.MainActivity
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityOpenBinding

/**
 *  description :
 */
class OpenActivity: BasicActivity<ActivityOpenBinding>() {

    override fun vBinding(): ActivityOpenBinding {
        return ActivityOpenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startProgress {
            openMain()
        }
    }

    private fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private var valueAni: ValueAnimator? = null
    private fun startProgress(actionDone: () -> Unit) {
        valueAni?.cancel()
        valueAni = ValueAnimator.ofInt(binding.progressBar.progress, 100)
        valueAni?.duration = 3000L
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