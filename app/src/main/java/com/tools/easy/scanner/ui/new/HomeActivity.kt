package com.tools.easy.scanner.ui.new

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.os.RemoteException
import android.view.View
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
    private val connection = ShadowsocksConnection(true)

    private var vEnabled = false
    private fun checkV() {
        vEnabled = ReferSupport.isCurrentStateEnabled()
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
        connection.connect(this, this)
        mRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        mRotateReverse = AnimationUtils.loadAnimation(this, R.anim.rotate_reverse)

        checkV()
        mConnServerEntity = AppConfig.ins.cachedServer
        reviewCurrentServer()
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
        updateConnUI(state)
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
                    ad.show(this@HomeActivity)
                }
                Core.stopService()
                ConnResultActivity.open(this@HomeActivity, false, connectedServer)
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
                    connectedServer = serverEntity!!
                    Core.startService()
                    ConnResultActivity.open(this@HomeActivity, true, serverEntity)
                } else {
                    changeState(BaseService.State.Stopped)
                    toastLong("Connect failed, try again")
                }

                val intersAd = ad
                if (intersAd is BaseInterstitial) {
                    intersAd.show(this@HomeActivity)
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
                    ad.show(this@HomeActivity)
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


}