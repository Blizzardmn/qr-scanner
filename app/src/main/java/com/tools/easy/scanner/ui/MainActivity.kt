package com.tools.easy.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.GridLayoutManager
import com.tools.easy.scanner.App
import com.tools.easy.scanner.R
import com.tools.easy.scanner.advertise.AdConst
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.advertise.base.*
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityMainBinding
import com.tools.easy.scanner.support.GpSupport
import com.tools.easy.scanner.support.Supports
import com.tools.easy.scanner.ui.adapter.CardAdapter
import com.tools.easy.scanner.ui.generate.CreateActivity
import com.tools.easy.scanner.ui.other.HtmlActivity
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : BasicActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun vBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    data class TypeItem(@DrawableRes val icon: Int, val nameImg: Int, val name: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initList()

        binding.main.apply {
            imgScan.setOnClickListener(this@MainActivity)
            imgSettings.setOnClickListener(this@MainActivity)
        }
        binding.navView.apply {
            findViewById<TextView>(R.id.tv_rate).setOnClickListener(this@MainActivity)
            findViewById<TextView>(R.id.tv_privacy).setOnClickListener(this@MainActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_scan -> {
                startActivity(Intent(this, ScanActivity::class.java))
            }

            R.id.img_settings -> {
                binding.drawerLayout.openDrawer(binding.navView)
            }

            R.id.tv_rate -> {
                GpSupport.skip2Market(packageName)
            }

            R.id.tv_privacy -> {
                HtmlActivity.openPrivacy(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAd()
    }

    override fun onBackPressed() {
        if (closeDrawer()) return
        super.onBackPressed()
    }

    override fun onDestroy() {
        nativeAd?.onDestroy()
        super.onDestroy()
    }

    private fun closeDrawer(): Boolean {
        if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(binding.navView)
            return true
        }
        return false
    }

    //最后一次展示home_native的时间
    private var lastShowTime = 0L
    //原生廣告被點擊了
    private var isNativeClicked = false
    //首頁原生
    private var nativeAd: BaseNative? = null
    private fun loadAd() {
        if (atomicBackAd.getAndSet(false)) {
            AdLoader.loadAd(App.ins, AdConst.adBack, object : AdsListener() {
                override fun onAdLoaded(ad: BaseAd) {
                    if (ad !is BaseInterstitial) {
                        return
                    }
                    ad.show(this@MainActivity)
                }
            }, justCache = true)
        }
        AdLoader.preloadAd(AdConst.adProcess)
        AdLoader.preloadAd(AdConst.adResult)
        AdLoader.preloadAd(AdConst.adBack)
        //nativeAd
        //1 minutes
        if ((System.currentTimeMillis() - lastShowTime > 60_000L)
            || isNativeClicked) {
            isNativeClicked = false
            AdLoader.loadAd(App.ins, AdConst.adMain, object : AdsListener(){
                override fun onAdLoaded(ad: BaseAd) {
                    when (ad) {
                        is BaseNative -> {
                            nativeAd?.onDestroy()
                            nativeAd = ad
                            onNativeLoaded(ad)
                        }

                        else -> return
                    }

                    lastShowTime = System.currentTimeMillis()
                }

                override fun onAdClick() {
                    isNativeClicked = true
                }

                override fun onAdError(err: String?) {
                    onNativeLoaded(null)
                }
            })
        }
    }

    private fun onNativeLoaded(ad: BaseNative?) {
        if (ad !is AdmobNative) {
            binding.main.adHold.visibility = View.VISIBLE
            val adContainer = binding.main.adMain.root
            adContainer.visibility = View.GONE
            return
        }

        binding.main.adHold.visibility = View.GONE
        binding.main.adMain.root.apply {
            visibility = View.VISIBLE

            ad.showIcon(this, findViewById(R.id.ad_icon))
            ad.showImage(this, findViewById(R.id.ad_img))
            ad.showTitle(this, findViewById(R.id.ad_title))
            ad.showBody(this, findViewById(R.id.ad_desc))
            ad.showCta(this, findViewById(R.id.ad_action))
            ad.register(this)
        }

        AdLoader.preloadAd(AdConst.adMain)
    }

    private fun initList() {
        val list = arrayListOf(
            TypeItem(R.mipmap.ic_home_instagram, R.mipmap.hm_instagram, Supports.catInstagram),
            TypeItem(R.mipmap.ic_home_facebook, R.mipmap.hm_facebook, Supports.catFacebook),
            TypeItem(R.mipmap.ic_home_twitter, R.mipmap.hm_twitter, Supports.catTwitter),
            TypeItem(R.mipmap.ic_home_youtube, R.mipmap.hm_youtube, Supports.catYoutube),
            TypeItem(R.mipmap.ic_home_whatsapp, R.mipmap.hm_whatsapp, Supports.catWhatsapp),
            TypeItem(R.mipmap.ic_home_tiktok, R.mipmap.hm_tiktok, Supports.catTiktok),
            TypeItem(R.mipmap.ic_home_clipboard, R.mipmap.hm_clipboard, Supports.catClipboard),
            TypeItem(R.mipmap.ic_home_email, R.mipmap.hm_email, Supports.catEmail),
            TypeItem(R.mipmap.ic_home_website, R.mipmap.hm_websites, Supports.catWebsite),
            TypeItem(R.mipmap.ic_home_text, R.mipmap.hm_text, Supports.catText),
            /*TypeItem(R.mipmap.ic_home_contacts, R.mipmap.hm_contacts, Supports.catContacts),
            TypeItem(R.mipmap.ic_home_wifi, R.mipmap.hm_wifi, Supports.catWifi)*/
        )
        binding.main.recyclerView.layoutManager = GridLayoutManager(this, 2)
        val adapter = CardAdapter(this, list)
        binding.main.recyclerView.adapter = adapter
        adapter.setOnClick {
            CreateActivity.openCreatePage(this@MainActivity, it.name)
        }
    }

    companion object {
        val atomicBackAd = AtomicBoolean(false)
    }
}