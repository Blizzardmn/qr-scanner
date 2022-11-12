package com.tools.easy.scanner.ui.new

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.os.RemoteException
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
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
import com.tools.easy.scanner.databinding.ActivityHomeBinding
import com.tools.easy.scanner.datas.AppConfig
import com.tools.easy.scanner.datas.AppEventLogger
import com.tools.easy.scanner.datas.DataStorage
import com.tools.easy.scanner.datas.entity.ServerEntity
import com.tools.easy.scanner.support.GpSupport
import com.tools.easy.scanner.support.ReferSupport
import com.tools.easy.scanner.ui.ScanActivity
import com.tools.easy.scanner.ui.home.ConnResultActivity
import com.tools.easy.scanner.ui.other.HtmlActivity
import com.tools.easy.scanner.ui.widget.MaskView
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 *  description :
 */
class HomeActivity: BasicActivity<ActivityHomeBinding>(), View.OnClickListener, ShadowsocksConnection.Callback {

    override fun vBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }
    private lateinit var mRotate: Animation
    private lateinit var mRotateReverse: Animation

    private val reqPermissionCode = 10000
    private var maskView: MaskView? = null
    private val vConnection = ShadowsocksConnection(true)

    private var vEnabled = false
    private fun checkV() {
        vEnabled = ReferSupport.isCurrentStateEnabled()
        AppEventLogger.ins.logEvent(if(vEnabled) "open_main_refer_user" else "open_main_referno_user")
    }

    companion object {
        val atomicBackAd = AtomicBoolean(false)
        var connectedServer: ServerEntity = ServerEntity(true)
    }

    private var mConnServerEntity = connectedServer

    private val openServersLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult == null) return@registerForActivityResult
        if (activityResult.resultCode == Activity.RESULT_OK) {
            if (Core.isConnecting()) {
                toastLong("Please wait operation done.")
                return@registerForActivityResult
            }
            /*val serverEntity = activityResult.data?.getSerializableExtra(ServKey) as? ConnBean
                ?: return@registerForActivityResult*/
            mConnServerEntity = connectedServer
            reviewCurrentServer()
            AppConfig.ins.cachedServer = mConnServerEntity
            doConnect()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vConnection.connect(this, this)
        mRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        mRotateReverse = AnimationUtils.loadAnimation(this, R.anim.rotate_reverse)

        checkV()
        mConnServerEntity = AppConfig.ins.cachedServer
        reviewCurrentServer()

        if (AppConfig.ins.isFirstInApp) {
            AppConfig.ins.isFirstInApp = false
            if (maskView == null) {
                maskView = MaskView(this)
                val root = (window.decorView) as ViewGroup
                maskView!!.setBackgroundColor(Color.parseColor("#AD000000"))
                root.addView(maskView, ViewGroup.LayoutParams(-1, -1))
                maskView!!.setView(binding.main.imgConn) {
                    doConnect()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkV()
    }

    @SuppressLint("SetTextI18n")
    private fun reviewCurrentServer() {
        if (mConnServerEntity.isFaster) {
            binding.main.tvServ.text = mConnServerEntity.name
            binding.main.imgServ.setImageResource(R.drawable.serv_fast)
            return
        }

        binding.main.tvServ.text = mConnServerEntity.name
        binding.main.imgServ.setImageResource(App.servFlagByCode(mConnServerEntity.code))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_scan -> {
                startActivity(Intent(this, ScanActivity::class.java))
            }

            R.id.bg_serv -> {
                val intent = Intent(this, ServersActivity::class.java)
                openServersLauncher.launch(intent)
            }

            R.id.img_conn, R.id.tv_conn -> doConnect()

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
        checkStartTimer()
    }

    override fun onStop() {
        super.onStop()
        jobTimer?.cancel()
    }

    override fun onBackPressed() {
        if (closeDrawer()) return
        super.onBackPressed()
    }

    override fun onDestroy() {
        nativeAd?.onDestroy()
        vConnection.disconnect(this)
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
        updateConnUI(state)
    }

    override fun onBinderDied() {
        vConnection.disconnect(this)
        vConnection.connect(this, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            reqPermissionCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    startConnect(mConnServerEntity)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateConnUI(state: BaseService.State) {
        binding.main.apply {

            if (state == BaseService.State.Connected) {
                imgConn.setImageResource(R.mipmap.bg_conn_btn_done)
                parent.setBackgroundColor(Color.parseColor("#EFFFFB"))
                bgBottom.setBackgroundResource(R.drawable.gradient_dbfef4_rt140)
                bgServ.setBackgroundResource(R.drawable.bg_19daa3_r8)
                bgConn.setBackgroundResource(R.drawable.bg_19daa3_r8)
                tvConn.setBackgroundResource(R.drawable.bg_101a34_r8)
                tvConn.setTextColor(Color.parseColor("#FFFFFF"))
                tvTime.text = "00:00:00"
                AppConfig.ins.connectedTimeMs = System.currentTimeMillis()
                checkStartTimer()
            } else {
                imgConn.setImageResource(R.mipmap.bg_conn_btn)
                parent.setBackgroundColor(Color.parseColor("#FFFFFF"))
                bgBottom.setBackgroundResource(R.drawable.gradient_33dfe1e8_rt140)
                bgServ.setBackgroundResource(R.drawable.stroke_12ce98_r12)
                bgConn.setBackgroundResource(R.drawable.stroke_12ce98_r12)
                tvConn.setBackgroundResource(R.drawable.bg_19daa3_r8)
                tvConn.setTextColor(Color.parseColor("#111A34"))
                tvTime.text = "__:__:__"
            }

            when(state) {
                BaseService.State.Connected -> {
                    tvConn.text = "Connected"
                    connRotate.clearAnimation()
                    connRotate.visibility = View.GONE
                }

                BaseService.State.Connecting -> {
                    tvConn.text = "Connecting"
                    connRotate.visibility = View.VISIBLE
                    connRotate.startAnimation(mRotate)
                }

                BaseService.State.Stopping -> {
                    tvConn.text = "Stopping"
                    connRotate.visibility = View.VISIBLE
                    connRotate.startAnimation(mRotateReverse)
                }

                else -> {
                    tvConn.text = "Connect"
                    connRotate.clearAnimation()
                    connRotate.visibility = View.GONE
                }
            }
        }
    }

    private var jobTimer: Job? = null
    private var startTms = 0L
    private fun checkStartTimer() {
        if (!Core.isConnected()) return
        jobTimer?.cancel()
        jobTimer = lifecycleScope.launch {
            startTms = AppConfig.ins.connectedTimeMs

            var h: Long
            var m: Long
            var s: Long
            var l: Long
            while (!isActivityPaused()) {
                l = (System.currentTimeMillis() - startTms) / 1000L
                h = l / 3600
                l %= 3600
                m = l / 60
                s = l % 60
                binding.main.tvTime.text = String.format("%02d:%02d:%02d", h, m, s)

                delay(1000L)
            }
        }
    }

    private fun doConnect() {
        if (Core.isConnecting()) return
        if (!checkVpnPermission()) return
        startConnect(mConnServerEntity)
    }

    private fun checkVpnPermission(): Boolean {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, reqPermissionCode)
            return false
        }
        return true
    }
    
    private fun dispatch2Result(isConn: Boolean) {
        if (!App.ins.isAppForeground()) return
        ConnResultActivity.open(this, isConn, mConnServerEntity)
    }
    
    private fun onConnectFailed() {
        toastLong("Connect Failed, Try again")
        AppEventLogger.ins.logEvent("connect_failed")
    }

    private fun currentTms(): Long {
        return System.nanoTime() / 1000_000L
    }

    private var connectAd: BaseAd? = null
    private var isConnect = false
    //connect 连接流程完了
    private var adLogicOver = false
    private val connListener = object :AdsListener(){
        override fun onAdLoaded(ad: BaseAd) {
            if (ad !is AdmobInterstitial) return
            if (!App.ins.isAppForeground()) {
                AdLoader.add2cache(AdConst.adIns, ad)
                return
            }
            connectAd = ad
            if (adLogicOver) {
                AdLoader.add2cache(AdConst.adIns, ad)
            }
        }

        override fun onAdDismiss() {
            handleClose()
        }
    }

    private fun handleClose() {
        if (isConnect) {
            handleConnect()
        } else {
            dispatch2Result(false)
        }
    }

    private fun loadConnectAd() {
        connectAd = null
        adLogicOver = false
        AdLoader.loadAd(App.ins, AdConst.adIns, connListener)
    }

    //连接下一步
    private fun handleConnect() {
        //准备好账户直接连接
        if (isAccountPrepared) {
            AppEventLogger.ins.logEvent("connect_success")
            dispatch2Result(true)
        } else {
            onConnectFailed()
            updateConnUI(BaseService.State.Stopped)
        }
    }

    private fun externalCheckAd() {
        //再额外的检查一次
        if (connectAd == null) {
            val cache = AdLoader.getCache(AdConst.adIns)
            if (cache != null) {
                cache.defineListener(connListener)
                connListener.onAdLoaded(cache)
            }
        }
    }

    private var isAccountPrepared = false

    private fun startConnect(server: ServerEntity) {
        maskView?.visibility = View.GONE
        AdLoader.preloadAd(AdConst.adResult)
        
        lifecycleScope.launch {
            if (Core.isConnected()) {
                isConnect = false
                updateConnUI(BaseService.State.Stopping)
                launch {
                    loadConnectAd()
                    val waits = 10//RemoteConfigs.getDisconnectTime() / 1000
                    delay(2000L)
                    var counter = 2
                    while (connectAd == null && counter++ < waits) {
                        delay(1000L)
                    }
                    //检查应用是否在前台，不在就取消操作
                    if (isActivityPaused()) {
                        connectAd?.let {
                            AdLoader.add2cache(AdConst.adIns, it)
                        }
                        vConnection.service?.apply { onServiceConnected(this) }
                        return@launch
                    }

                    externalCheckAd()
                    when (connectAd) {
                        is AdmobInterstitial -> {
                            if ((connectAd as? AdmobInterstitial)?.show(this@HomeActivity) == false) {
                                handleClose()
                            }
                        }
                        else -> {
                            handleClose()
                        }
                    }
                    adLogicOver = true

                    stopVPN()
                }
                return@launch
            }

            isConnect = true
            updateConnUI(BaseService.State.Connecting)
            AppEventLogger.ins.logEvent("connect_start")
            launch {
                loadConnectAd()
                isAccountPrepared = false
                val startTms = currentTms()
                val connectTms = 10000L//RemoteConfigs.getConnectTime()
                delay(2000L)

                val awaitAccount = async {
                    val isPrepared = withTimeoutOrNull(connectTms) {
                        //Log.i("dsfafa", "smartConn.await()")
                        isAccountPrepared = if (server.isFaster) startAutoConnecting() else startChoiceConnecting(server)
                        //Log.i("dsfafa", "smartConn $isAccountPrepared")
                        isAccountPrepared
                    }
                    isPrepared ?: false
                }

                //假象
                val awaitAds = async(Dispatchers.Main) {
                    val isLoaded = withTimeoutOrNull(3500) {
                        //Log.i("dsfafa", "awaitAds.await()")
                        true
                    }
                    isLoaded ?: false
                }

                awaitAccount.await() && awaitAds.await()
                val cost = currentTms() - startTms
                if (cost < connectTms) {
                    delay(connectTms - cost)
                }
                //检查应用是否在前台，不在就取消操作
                if (isActivityPaused()) {
                    connectAd?.let {
                        AdLoader.add2cache(AdConst.adIns, it)
                    }
                    vConnection.service?.apply { onServiceConnected(this) }
                    return@launch
                }
                externalCheckAd()
                adLogicOver = true
                when (connectAd) {
                    is AdmobInterstitial -> {
                        if ((connectAd as? AdmobInterstitial)?.show(this@HomeActivity) == false) {
                            handleConnect()
                        }
                    }

                    else -> {
                        handleConnect()
                    }
                }

                if (isAccountPrepared) { //准备好账户直接连接
                    //FirebaseEvents.impl.sendEvent("connect_success")
                    startVPN()
                }
            }
        }
    }
    
    private suspend fun startAutoConnecting(): Boolean = suspendCancellableCoroutine { const ->
        runBlocking {
            DataStorage.startCheck {
                if (isActivityPaused()) {
                    changeState(BaseService.State.Stopped)
                    const.resume(false)
                    return@startCheck
                }

                if (it != null) {
                    defineServer(it)
                    const.resume(true)
                } else {
                    changeState(BaseService.State.Stopped)
                    const.resume(false)
                }
            }
        }
    }

    private suspend fun startChoiceConnecting(serverEntity: ServerEntity): Boolean = suspendCancellableCoroutine {
        if (!serverEntity.isValid()) {
            it.resume(false)
            return@suspendCancellableCoroutine
        }
        defineServer(serverEntity)
        if (it.isActive)
            it.resume(true)
    }

    private fun defineServer(serverEntity: ServerEntity) {
        val profile = Profile()
        profile.host = serverEntity.host
        profile.remotePort = serverEntity.port
        profile.password = serverEntity.pwd
        Core.currentProfile = ProfileManager.expand(profile)
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
                    ad.show(this@HomeActivity)
                }
            }, justCache = true)
        }
        if (vEnabled) AdLoader.preloadAd(AdConst.adBack)
        AdLoader.preloadAd(AdConst.adIns)
        //nativeAd
        //1 minutes
        if ((System.currentTimeMillis() - lastShowTime > 50_000L)
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

    private fun startVPN() {
        Core.startService()
    }

    private fun stopVPN() {
        Core.stopService()
    }


}