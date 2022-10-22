package com.tools.easy.scanner.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import com.github.shadowsocks.bg.BaseService
import com.tools.easy.scanner.R
import java.nio.file.Files.find

/**
 *  description :
 */
class ConnBtn: RelativeLayout {
    private var curState = BaseService.State.Idle
    private lateinit var imgBg: View
    private lateinit var imgNormal: ImageView
    private lateinit var imgDoing: ImageView
    private lateinit var imgConnected: ImageView

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        inflate(context, R.layout.layout_conn_btn, this)
        imgBg = findViewById(R.id.img_bg)
        imgNormal = findViewById(R.id.img_normal)
        imgDoing = findViewById(R.id.img_doing)
        imgConnected = findViewById(R.id.img_connected)
    }

    constructor(context: Context, attrs: AttributeSet, def: Int) : this(context, attrs)

    fun changeState(state: BaseService.State) {
        if (curState == state) return

        curState = state
        when (state) {
            BaseService.State.Idle, BaseService.State.Stopped -> {
                imgDoing.clearAnimation()
                imgDoing.visibility = View.GONE
                imgNormal.visibility = View.VISIBLE
                imgConnected.visibility = View.GONE
            }

            BaseService.State.Connecting, BaseService.State.Stopping -> {
                startRotate()
                imgDoing.visibility = View.VISIBLE
                imgNormal.visibility = View.GONE
                imgConnected.visibility = View.GONE
            }

            BaseService.State.Connected -> {
                imgDoing.clearAnimation()
                imgDoing.visibility = View.GONE
                imgNormal.visibility = View.GONE
                imgConnected.visibility = View.VISIBLE
            }
        }
    }

    private fun startRotate() {
        val anim = RotateAnimation(0f, 360f, 1, 0.5f, 1, 0.5f)
        anim.interpolator = LinearInterpolator()
        anim.duration = 1200
        anim.repeatCount = -1
        imgDoing.startAnimation(anim)
    }
}