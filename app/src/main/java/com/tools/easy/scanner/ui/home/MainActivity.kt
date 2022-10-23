package com.tools.easy.scanner.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.os.RemoteException
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.tools.easy.scanner.App
import com.tools.easy.scanner.R
import com.tools.easy.scanner.advertise.AdConst
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.advertise.base.*
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityMainBinding
import com.tools.easy.scanner.datas.AppConfig
import com.tools.easy.scanner.datas.DataStorage
import com.tools.easy.scanner.datas.entity.ServerEntity
import com.tools.easy.scanner.support.GpSupport
import com.tools.easy.scanner.support.ReferSupport
import com.tools.easy.scanner.support.Supports
import com.tools.easy.scanner.ui.ScanActivity
import com.tools.easy.scanner.ui.adapter.CardAdapter
import com.tools.easy.scanner.ui.generate.CreateActivity
import com.tools.easy.scanner.ui.other.HtmlActivity
import com.tools.easy.scanner.ui.widget.MaskView
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

class MainActivity : BasicActivity<ActivityMainBinding>(), View.OnClickListener, ShadowsocksConnection.Callback {

    override fun vBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    data class TypeItem(@DrawableRes val icon: Int, val nameImg: Int, val name: String)
    private val connection = ShadowsocksConnection(true)
    private val reqPermissionCode = 10000

    private lateinit var homeModel: HomeViewModel
    private var maskView: MaskView? = null
    private var vEnabled = false
    private fun checkV() {
        vEnabled = ReferSupport.isCurrentStateEnabled()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkV()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        connection.connect(this, this)

        initList()
        checkV()

        binding.main.apply {
            imgScan.setOnClickListener(this@MainActivity)
            imgSettings.setOnClickListener(this@MainActivity)
            imgV.setOnClickListener(this@MainActivity)
        }
        binding.navView.apply {
            findViewById<TextView>(R.id.tv_rate).setOnClickListener(this@MainActivity)
            findViewById<TextView>(R.id.tv_privacy).setOnClickListener(this@MainActivity)
        }

        if (!vEnabled) return
        binding.main.groupV.visibility = View.VISIBLE
        if (AppConfig.ins.isFirstInApp) {
            AppConfig.ins.isFirstInApp = false
            if (maskView == null) {
                maskView = MaskView(this)
                val root = (window.decorView) as ViewGroup
                maskView!!.setBackgroundColor(Color.parseColor("#E0000000"))
                root.addView(maskView, ViewGroup.LayoutParams(-1, -1))
                maskView!!.setView(binding.main.imgV) {
                    doConnect()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_scan -> {
                startActivity(Intent(this, ScanActivity::class.java))
            }

            R.id.img_v -> doConnect()

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
        connection.disconnect(this)
        super.onDestroy()
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        changeState(state)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = try {
            BaseService.State.values()[service.state]
        } catch (_: RemoteException) {
            BaseService.State.Idle
        }
        changeState(state, true)
    }

    private fun changeState(state: BaseService.State, isInit: Boolean = false) {
        if (Core.curState == state && !isInit) return

        Core.curState = state
        binding.main.connBtn.changeState(state)
    }

    override fun onBinderDied() {
        connection.disconnect(this)
        connection.connect(this, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            reqPermissionCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    startConnect()
                }
            }
        }
    }

    private fun doConnect() {
        if (Core.isConnecting()) return
        if (!checkVpnPermission()) return
        startConnect()
    }

    private fun checkVpnPermission(): Boolean {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, reqPermissionCode)
            return false
        }
        return true
    }

    private fun startConnect() {
        maskView?.visibility = View.GONE
        lifecycleScope.launch {
            if (Core.curState == BaseService.State.Connected) {
                changeState(BaseService.State.Stopping)
                val ad = withTimeoutOrNull(10000) {
                    loadConnectAd()
                }

                if (isActivityPaused()) {
                    changeState(BaseService.State.Connected)
                    return@launch
                }

                if (ad is BaseInterstitial) {
                    ad.show(this@MainActivity)
                }
                Core.stopService()
                ConnResultActivity.open(this@MainActivity, false, connectedServer)
            } else {
                changeState(BaseService.State.Connecting)
                var serverEntity: ServerEntity? = null
                var ad: BaseAd? = null

                val adAwait = async {
                    withTimeoutOrNull(10000) {
                        ad = loadConnectAd()
                        10
                    } ?: 10
                }

                val connectAwait = async {
                    serverEntity = connectTest()
                    10
                }

                adAwait.await() > 5 && connectAwait.await() > 5
                if (isActivityPaused()) {
                    ad?.apply {
                        AdLoader.add2cache(AdConst.adIns, this)
                    }
                    changeState(BaseService.State.Stopped)
                    return@launch
                }

                if (serverEntity != null) {
                    val profile = Profile()
                    profile.host = serverEntity!!.host
                    profile.remotePort = serverEntity!!.port
                    profile.password = serverEntity!!.pwd
                    Core.currentProfile = ProfileManager.expand(profile)
                    connectedServer = serverEntity
                    Core.startService()
                    ConnResultActivity.open(this@MainActivity, true, serverEntity)
                } else {
                    changeState(BaseService.State.Stopped)
                    toastLong("Connect failed, try again")
                }

                val intersAd = ad
                if (intersAd is BaseInterstitial) {
                    intersAd.show(this@MainActivity)
                }
            }

        }
    }

    private suspend fun connectTest(): ServerEntity? = suspendCancellableCoroutine { const ->
        runBlocking {
            DataStorage.startCheck {
                if (isActivityPaused()) {
                    changeState(BaseService.State.Stopped)
                    const.resume(null)
                    return@startCheck
                }

                if (it != null) {
                    const.resume(it)
                } else {
                    changeState(BaseService.State.Stopped)
                    const.resume(null)
                }
            }
        }
    }

    private suspend fun loadConnectAd(): BaseAd? = suspendCancellableCoroutine {
        AdLoader.loadAd(App.ins, AdConst.adIns, object : AdsListener() {
            var isResumed = false

            override fun onAdLoaded(ad: BaseAd) {
                if (!isResumed) {
                    isResumed = true
                    it.resume(ad)
                }
            }

            override fun onAdError(err: String?) {
                if (!isResumed) {
                    isResumed = true
                    it.resume(null)
                }
            }
        })
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
        if (vEnabled && atomicBackAd.getAndSet(false)) {
            AdLoader.loadAd(App.ins, AdConst.adBack, object : AdsListener() {
                override fun onAdLoaded(ad: BaseAd) {
                    if (ad !is BaseInterstitial) {
                        return
                    }
                    ad.show(this@MainActivity)
                }
            }, justCache = true)
        }
        AdLoader.preloadAd(AdConst.adIns)
        AdLoader.preloadAd(AdConst.adResult)
        if (vEnabled) AdLoader.preloadAd(AdConst.adBack)
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
        var connectedServer: ServerEntity? = null
    }
}