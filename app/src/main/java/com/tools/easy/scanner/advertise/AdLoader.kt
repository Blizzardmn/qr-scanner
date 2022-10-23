package com.tools.easy.scanner.advertise

import android.content.Context
import com.tools.easy.scanner.App
import com.tools.easy.scanner.advertise.base.AdPos
import com.tools.easy.scanner.advertise.base.AdsListener
import com.tools.easy.scanner.advertise.base.BaseAd
import com.tools.easy.scanner.advertise.conf.ConfigId
import com.tools.easy.scanner.advertise.conf.ConfigPos
import com.tools.easy.scanner.datas.RemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *  description :
 */
object AdLoader: AdmobLoader(), CoroutineScope by MainScope() {

    private val cacheAds = HashMap<String, ArrayList<BaseAd>>()

    private val comparator = object : Comparator<BaseAd> {
        override fun compare(o1: BaseAd?, o2: BaseAd?): Int {
            if (o1 == null || o2 == null) return 0
            return o1.configId.weight.compareTo(o2.configId.weight) * -1
        }
    }

    @Synchronized
    fun getCache(adPos: AdPos): BaseAd? {
        val arrayList = cacheAds[adPos.name] ?: return null
        if (arrayList.isEmpty()) return null
        return arrayList.removeAt(0)
    }

    @Synchronized
    fun add2cache(adPos: AdPos, ad: BaseAd) {
        var arrayList = cacheAds[adPos.name]
        if (arrayList == null) {
            arrayList = arrayListOf()
            cacheAds[adPos.name] = arrayList
        }
        arrayList.add(ad)
        Collections.sort(arrayList, comparator)
    }

    @Synchronized
    fun hasCached(adPos: AdPos): Boolean {
        val arrayList = cacheAds[adPos.name] ?: return false
        return !arrayList.isNullOrEmpty()
    }

    fun preloadAd(adPos: AdPos) {
        if (hasCached(adPos)) return
        loadAd(App.ins, adPos, object : AdsListener() {
            override fun onAdLoaded(ad: BaseAd) {
                add2cache(adPos, ad)
            }
        }, forceLoad = false)
    }

    fun loadAd(ctx: Context, adPos: AdPos, adsListener: AdsListener, justCache: Boolean = false, forceLoad: Boolean = true) {
        val cache = getCache(adPos)
        if (cache != null) {
            cache.defineListener(adsListener)
            adsListener.onAdLoaded(cache)
            return
        }
        if (justCache) {
            adsListener.onAdError("noCache")
            return
        }
        if (!forceLoad && adPos.isRequesting) {
            return
        }

        var configPos = synchronized(adsConfig) {
            adsConfig[adPos.name]
        }
        if (configPos == null || configPos.isEmpty()) {
            parseLocalConfig()
        }
        configPos = synchronized(adsConfig) {
            adsConfig[adPos.name]
        }
        if (configPos == null || configPos.isEmpty()) {
            adsListener.onAdError("noConfig")
            return
        }

        val lists = arrayListOf<ConfigId>()
        lists.addAll(configPos.ids)

        adPos.isRequesting = true
        launch { traversalId(ctx.applicationContext, adPos, lists, adsListener) }
    }

    private fun traversalId(ctx: Context, adPos: AdPos, configIds: ArrayList<ConfigId>, adsListener: AdsListener) {
        if (configIds.isNullOrEmpty()) {
            adPos.isRequesting = false
            adsListener.onAdError("Request All Done")
            return
        }

        fun checkIt(ad: BaseAd?) {
            if (ad != null) {
                adPos.isRequesting = false
                ad.defineListener(adsListener)
                adsListener.onAdLoaded(ad)
                return
            }
            traversalId(ctx, adPos, configIds, adsListener)
        }

        val removeAt = configIds.removeAt(0)
        when (removeAt.type) {
            "opn" -> loadOpen(ctx, adPos, removeAt) {
                checkIt(it)
            }

            "ins" -> loadInterstitial(ctx, adPos, removeAt) {
                checkIt(it)
            }

            "nav" -> loadNative(ctx, adPos, removeAt) {
                checkIt(it)
            }

            else -> traversalId(ctx, adPos, configIds, adsListener)
        }
    }

    private val adsConfig = HashMap<String, ConfigPos>()
    //private var dailyShownUpper = 30
    //private var dailyClickUpper = 8

    fun parseRemoteConfig() {
        val remoteAdConfig = RemoteConfig.ins.getAdConfig()
        if (remoteAdConfig.isNullOrEmpty()) return
        dealtParse(remoteAdConfig)
    }

    fun parseLocalConfig() {
        dealtParse(local)
    }

    @Synchronized
    private fun dealtParse(adConfigString: String) {
        val adsList = HashMap<String, ConfigPos>()

        fun parsePosition(adPos: AdPos, jsonArray: JSONArray?) {
            if (jsonArray == null) return
            val listIds = arrayListOf<ConfigId>()
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.optJSONObject(i) ?: continue
                listIds.add(
                    ConfigId(
                        id = json.optString("id"),
                        type = json.optString("tp"),
                        weight = json.optInt("wt")
                    )
                )
            }
            listIds.sortBy { it.weight * -1 }
            adsList[adPos.name] = ConfigPos(adPos, listIds)
        }

        try {
            val jsonObject = JSONObject(adConfigString)
            /*dailyShownUpper = jsonObject.optInt("iTran_zks", 30)
            dailyClickUpper = jsonObject.optInt("iTran_ydj", 10)*/

            parsePosition(AdConst.adOpen, jsonObject.optJSONArray(AdConst.adOpen.name))
            parsePosition(AdConst.adIns, jsonObject.optJSONArray(AdConst.adIns.name))
            parsePosition(AdConst.adMain, jsonObject.optJSONArray(AdConst.adMain.name))
            parsePosition(AdConst.adResult, jsonObject.optJSONArray(AdConst.adResult.name))
            parsePosition(AdConst.adBack, jsonObject.optJSONArray(AdConst.adBack.name))
        } catch (e: Exception) {}

        synchronized(adsConfig) {
            adsConfig.clear()
            adsConfig.putAll(adsList)
        }
    }

    private const val local = "{\n" +
            "    \"open\":[\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/4118224971\",\n" +
            "            \"tp\":\"opn\",\n" +
            "            \"wt\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/8223311936\",\n" +
            "            \"tp\":\"opn\",\n" +
            "            \"wt\":1\n" +
            "        }\n" +
            "    ],\n" +
            "    \"ins_process\":[\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/4913281959\",\n" +
            "            \"tp\":\"ins\",\n" +
            "            \"wt\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/7721297796\",\n" +
            "            \"tp\":\"ins\",\n" +
            "            \"wt\":1\n" +
            "        }\n" +
            "    ],\n" +
            "    \"ins_back\":[\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/3600200285\",\n" +
            "            \"tp\":\"ins\",\n" +
            "            \"wt\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/8105571444\",\n" +
            "            \"tp\":\"ins\",\n" +
            "            \"wt\":1\n" +
            "        }\n" +
            "    ],\n" +
            "    \"nav_main\":[\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/2805143309\",\n" +
            "            \"tp\":\"nav\",\n" +
            "            \"wt\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/4663340873\",\n" +
            "            \"tp\":\"nav\",\n" +
            "            \"wt\":1\n" +
            "        }\n" +
            "    ],\n" +
            "    \"nav_result\":[\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/8660955276\",\n" +
            "            \"tp\":\"nav\",\n" +
            "            \"wt\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\":\"ca-app-pub-1903241164241473/6408216128\",\n" +
            "            \"tp\":\"nav\",\n" +
            "            \"wt\":1\n" +
            "        }\n" +
            "    ]\n" +
            "}"
}