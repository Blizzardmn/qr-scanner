package com.tools.easy.scanner

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.datas.RemoteConfig

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

    inner class ActivityLife: ActivityLifecycleCallbacks {
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        }

        override fun onActivityStarted(p0: Activity) {
        }

        override fun onActivityResumed(p0: Activity) {
        }

        override fun onActivityPaused(p0: Activity) {
        }

        override fun onActivityStopped(p0: Activity) {
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityDestroyed(p0: Activity) {
        }
    }
}