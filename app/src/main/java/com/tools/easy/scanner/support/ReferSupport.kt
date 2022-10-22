package com.tools.easy.scanner.support

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.tools.easy.scanner.datas.AppConfig
import com.tools.easy.scanner.datas.DataStorage

/**
 *  description :
 */
object ReferSupport {

    var isUserFb = false
    var isUserOrganic = true
    var fetchSuccess = false

    fun isCurrentStateEnabled(): Boolean {
        val state = DataStorage.curState
        return when {
            state == 1 && isUserFb -> true

            state == 2 && isUserOrganic -> true

            else -> false
        }
    }

    fun referCheck(context: Context) {
        val refer = AppConfig.ins.installReferer
        if (!refer.isNullOrEmpty()) {
            analyzeUser(refer)
            return
        }
        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established.
                        val response: ReferrerDetails = referrerClient.installReferrer ?: return
                        val referrerUrl: String? = response.installReferrer
                        val referrerClickTime: Long = response.referrerClickTimestampSeconds
                        val appInstallTime: Long = response.installBeginTimestampSeconds
                        val instantExperienceLaunched: Boolean = response.googlePlayInstantParam

                        if (!referrerUrl.isNullOrEmpty()) {
                            analyzeUser(referrerUrl)
                            AppConfig.ins.installReferer = referrerUrl
                        }

                        referrerClient.endConnection()
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun analyzeUser(refer: String) {
        fetchSuccess = true
        when {
            refer.contains("fb4a") -> {
                isUserOrganic = false
                isUserFb = true
                //AppEventLogger.INSTANCE.sendEvent("refer_success_fb")
            }

            else -> {
                isUserFb = false
                isUserOrganic = true
                //AppEventLogger.INSTANCE.sendEvent("refer_success_organic")
            }
        }
    }
}