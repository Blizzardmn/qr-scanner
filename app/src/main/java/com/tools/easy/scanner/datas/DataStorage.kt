package com.tools.easy.scanner.datas

import android.util.Base64
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.tools.easy.scanner.datas.entity.ServerEntity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume

/**
 *  description :
 */
class DataStorage {

    companion object {
        //0 所有用户均不可见; 1: fb用户可见; 2: 自然量用户可见
        var curState = 0
        private val serverList = ArrayList<ServerEntity>()
        private const val DEFAULT_SERVERS =
            "eyJjdXJfc3RhdGUiOjEsInVzZXJfZW5hYmxlIjp0cnVlLCJyZWNvbW1lbmQiOiJVUyIsImxpc3QiOlt7ImlwIjoiMTkzLjM3LjU2LjEzOSIsInBvcnQiOjg4MjIsInB3ZCI6InhtbmlAI2NlbmRpc2c2MyIsImNvdW50cnkiOiJVbml0ZWQgS2luZ2RvbSAiLCJjaXR5IjoiTG9uZG9uMDEifV19"
    }

    fun startPing( result: (ipEntity: ServerEntity?) -> Unit) = GlobalScope.launch {
        val passList = Vector<ServerEntity>()
        val list = getIpList()
        list.forEach {
            launch(Dispatchers.IO) {
                if (ping(it)) passList.add(it)
            }
        }

        launch(Dispatchers.Main) {
            delay(2000L)
            if (passList.isNotEmpty()) {
                result(passList[Random().nextInt(passList.size)])
            } else {
                result(null)
            }
        }
    }

    private suspend fun ping(ipEntity: ServerEntity) = suspendCancellableCoroutine<Boolean> { const ->
        var bufferedReader: BufferedReader? = null
        var isOk = false
        try {
            val command = "ping -c 2 -w 2 ${ipEntity.host}"
            val process = Runtime.getRuntime().exec(command)
            if (process == null) {
                const.resume(false)
                return@suspendCancellableCoroutine
            }
            bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

            while (!isOk) {
                val line = bufferedReader.readLine() ?: break
                if (line.contains("time=") || line.contains("time<")) {
                    const.resume(true)
                    isOk = true
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bufferedReader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (!isOk) {
            const.resume(false)
        }
    }

    suspend fun getIpList(): ArrayList<ServerEntity> = suspendCancellableCoroutine { const ->
        synchronized(serverList) {
            if (serverList.isEmpty()) {
                getServers {
                    serverList.clear()
                    serverList.addAll(it)
                    const.resume(serverList)
                }
            } else {
                const.resume(serverList)
            }
        }
    }

    private fun getServers(result: (list: ArrayList<ServerEntity>) -> Unit) = GlobalScope.launch {
        var config = ""
        try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("bmw").document("the7")
            docRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    var str = it.result?.get("content")
                    if (str == null) str = DEFAULT_SERVERS
                    config = str as String
                } else {
                    it.exception?.message
                }

                if (config.isEmpty()) config = DEFAULT_SERVERS
                val jb = JSONObject(String(Base64.decode(config, Base64.NO_WRAP)))

                curState = jb.optInt("cur_state", 0)
                val serverArr = jb.getJSONArray("list")
                val list = ArrayList<ServerEntity>()
                for (i in 0 until serverArr.length()) {
                    val ipJb = serverArr.optJSONObject(i)
                    val ip = ipJb.optString("ip")
                    val port = ipJb.optInt("port")
                    val pwd = ipJb.optString("pwd")
                    val country = ipJb.optString("country")
                    val name = ipJb.optString("city")
                    val ipEntity = ServerEntity(country, name, ip, port, pwd)
                    list.add(ipEntity)
                }

                result(list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}