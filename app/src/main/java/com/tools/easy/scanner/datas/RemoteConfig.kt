package com.tools.easy.scanner.datas

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tools.easy.scanner.App
import com.tools.easy.scanner.R

/**
 *  description :
 */
class RemoteConfig {

    companion object {
        val ins: RemoteConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { RemoteConfig() }
    }

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        if (App.isDebug) {
            val minTs = 1 * 60L
            val configSetting = FirebaseRemoteConfigSettings.Builder()
                .setFetchTimeoutInSeconds(minTs).build()
            remoteConfig.setConfigSettingsAsync(configSetting)
        }
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    fun fetchAndActivate(action: () -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    action()
                }
            }
    }

    fun getAdConfig(): String {
        return remoteConfig.getString("revenue_what")
    }


}