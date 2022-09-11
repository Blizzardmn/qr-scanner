package com.tools.easy.scanner

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdActivity
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.datas.RemoteConfig
import com.tools.easy.scanner.ui.OpenActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        RemoteConfig.ins.fetchAndActivate {
            AdLoader.parseRemoteConfig()
        }
        registerActivityLifecycleCallbacks(ActivityLife())
    }

    //存在的前台Activity 数量
    private var nForeActivity = 0
    private var delayJob: Job? = null
    private var bHotLoading = false
    //屏蔽本次热启动
    private var blockHot = false

    fun blockOnceHot() {
        blockHot = true
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
                    && activity !is OpenActivity
                ) {
                    if (!blockHot && bHotLoading) {
                        OpenActivity.restart(activity)
                        //needFreshNav = false
                    }
                    blockHot = false
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
                delay(1100L)
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