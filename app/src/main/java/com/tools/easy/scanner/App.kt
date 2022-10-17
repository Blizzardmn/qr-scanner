package com.tools.easy.scanner

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.util.Log
import com.github.shadowsocks.Core
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.datas.RemoteConfig
import com.tools.easy.scanner.ui.MainActivity
import com.tools.easy.scanner.ui.OpenActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 *  description :
 */
class App: Application() {

    companion object {
        lateinit var ins: App
        var isFilter = false
        const val isDebug = true
    }

    override fun onCreate() {
        super.onCreate()
        ins = this
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Core.init(this, MainActivity::class)

        if (getProcessName(this) != packageName) return

        MobileAds.initialize(this)
        RemoteConfig.ins.fetchAndActivate {
            AdLoader.parseRemoteConfig()
        }
        registerActivityLifecycleCallbacks(ActivityLife())
    }

   private fun getProcessName(cxt: Context): String? {
        val pid = Process.myPid()
        val am = cxt.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses ?: return null
        for (procInfo in runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName
            }
        }
        return null
    }

    //存在的前台Activity 数量
    private var nForeActivity = 0
    private var delayJob: Job? = null
    private var bHotLoading = false
    //屏蔽本次热启动
    private var blockOne = false

    fun blockOne() {
        blockOne = true
    }

    fun isAppForeground(): Boolean {
        return nForeActivity > 0
    }

    @SuppressLint("LogNotTimber")
    private inner class ActivityLife: ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            Log.e("ActivityLife", "onActivityStarted: $activity")

            if (nForeActivity++ == 0) {
                delayJob?.cancel()
                if (activity !is AdActivity
                    && activity !is OpenActivity) {
                    if (!blockOne && bHotLoading) {
                        OpenActivity.restart(activity)
                        //needFreshNav = false
                    }
                    blockOne = false
                    bHotLoading = false
                }
            }
        }

        override fun onActivityResumed(activity: Activity) { }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            Log.i("ActivityLife", "onActivityStopped: $activity")
            --nForeActivity
            delayJob = GlobalScope.launch {
                delay(1800L)
                bHotLoading = true

                if (activity is AdActivity || (activity is OpenActivity && nForeActivity <= 0)) {
                    if (activity.isFinishing || activity.isDestroyed) return@launch
                    //if (!atomicBackHome.get()) return@launch
                    Log.e("ActivityLife", "finish: $activity")
                    activity.finish()
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            Log.i("ActivityLife", "onActivityDestroyed: $activity")
        }
    }
}