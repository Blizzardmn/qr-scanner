package com.tools.easy.scanner

import android.app.Application

/**
 *  description :
 */
class App: Application() {

    companion object {
        lateinit var ins: App
    }

    override fun onCreate() {
        super.onCreate()
        ins = this
    }

    var isFilter = false
}