package com.tools.easy.scanner.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.tools.easy.scanner.App
import com.tools.easy.scanner.R
import com.tools.easy.scanner.advertise.AdConst
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.advertise.base.AdmobNative
import com.tools.easy.scanner.advertise.base.AdsListener
import com.tools.easy.scanner.advertise.base.BaseAd
import com.tools.easy.scanner.advertise.base.BaseNative
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityConnResultBinding
import com.tools.easy.scanner.datas.entity.ServerEntity
import com.tools.easy.scanner.ui.ScanActivity

/**
 *  description :
 */
class ConnResultActivity: BasicActivity<ActivityConnResultBinding>(), View.OnClickListener {

    override fun vBinding(): ActivityConnResultBinding {
        return ActivityConnResultBinding.inflate(layoutInflater)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isConnect = intent.getBooleanExtra("is_yes", false)
        if (isConnect) {
            binding.imgType.setImageResource(R.mipmap.ic_connected)
            binding.tvTip.text = "Connected now"
        } else {
            binding.imgType.setImageResource(R.mipmap.ic_disconnected)
            binding.tvTip.text = "Disconnected now"
        }

        val serverEntity = intent.getSerializableExtra("server") as? ServerEntity
        binding.tvLocation.text = "${serverEntity?.country} - ${serverEntity?.name}"

        loadAd()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back -> onBackPressed()

            R.id.img_scan, R.id.btn_scan -> {
                startActivity(Intent(this, ScanActivity::class.java))
                finish()
            }
        }
    }

    override fun onBackPressed() {
        MainActivity.atomicBackAd.set(true)
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAd?.onDestroy()
    }

    private var nativeAd: BaseNative? = null
    private fun loadAd() {
        AdLoader.loadAd(App.ins, AdConst.adResult, object : AdsListener(){
            override fun onAdLoaded(ad: BaseAd) {
                when (ad) {
                    is BaseNative -> {
                        nativeAd?.onDestroy()
                        nativeAd = ad
                        onNativeLoaded(ad)
                    }

                    else -> return
                }
            }

            override fun onAdError(err: String?) {
                onNativeLoaded(null)
            }
        })
    }

    private fun onNativeLoaded(ad: BaseNative?) {
        if (ad !is AdmobNative) {
            binding.adHold.visibility = View.VISIBLE
            val adContainer = binding.adMain.root
            adContainer.visibility = View.GONE
            return
        }

        binding.adHold.visibility = View.GONE
        binding.adMain.root.apply {
            visibility = View.VISIBLE

            ad.showIcon(this, findViewById(R.id.ad_icon))
            ad.showImage(this, findViewById(R.id.ad_img))
            ad.showTitle(this, findViewById(R.id.ad_title))
            ad.showBody(this, findViewById(R.id.ad_desc))
            ad.showCta(this, findViewById(R.id.ad_action))
            ad.register(this)
        }
    }

    companion object {
        fun open(activity: Activity, isYes: Boolean, server: ServerEntity? = null) {
            val intent = Intent(activity, ConnResultActivity::class.java)
            intent.putExtra("is_yes", isYes)
            intent.putExtra("server", server)
            activity.startActivity(intent)
        }
    }
}