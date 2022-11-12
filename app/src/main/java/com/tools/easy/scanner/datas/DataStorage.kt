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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.coroutines.resume

/**
 *  description :
 */
object DataStorage {

    //0 所有用户均不可见; 1: fb用户可见; 2: 自然量用户可见
    var curState = 0
    private val serverList = ArrayList<ServerEntity>()
    private const val DEFAULT_SERVERS =
        "eyJjdXJfc3RhdGUiOjEsInVzZXJfZW5hYmxlIjp0cnVlLCJyZWNvbW1lbmQiOiJVUyIsImxpc3QiOlt7ImlwIjoiMTkzLjM3LjU2LjEzOSIsInBvcnQiOjg4MjIsInB3ZCI6InhtbmlAI2NlbmRpc2c2MyIsImNvdW50cnkiOiJVbml0ZWQgS2luZ2RvbSAiLCJjaXR5IjoiTG9uZG9uMDEifV19"

    private var dataList: ArrayList<ServerEntity>? = null

    /**
     * @param timeoutMs 实时获取的超时时间,超过这个时间点就直接返回local data
     * */
    fun getServers(action: (ArrayList<ServerEntity>?) -> Unit, timeoutMs: Long = 65_000L) = GlobalScope.launch(Dispatchers.Main) {
        //如果是已经获取到了及时的数据,那么就返回这个数据
        //testEncodeServers()
        if (!dataList.isNullOrEmpty()) {
            launch(Dispatchers.Main) {
                action.invoke(dataList)
            }
            return@launch
        }

        //超时之后return local data
        val atomicReturned = AtomicBoolean(false)
        fun onFailed2UsingLocalData() {
            if (!atomicReturned.getAndSet(true)) {
                val data = if (dataList.isNullOrEmpty()) {
                    obtainLocal()
                } else {
                    dataList
                }
                launch(Dispatchers.Main) {
                    action.invoke(data)
                }
            }
        }
        /*if (true) {
            onFailed2UsingLocalData()
            return
        }*/

        launch {
            delay(timeoutMs)
            onFailed2UsingLocalData()
        }

        getServers { testList ->
            if (testList.isNullOrEmpty()) {
                onFailed2UsingLocalData()
            } else {
                Collections.sort(testList, object :Comparator<ServerEntity> {
                    override fun compare(
                        o1: ServerEntity?,
                        o2: ServerEntity?
                    ): Int {
                        if (o1 == null || o2 == null) return 0
                        return o1.name.compareTo(o2.name)
                    }
                })
                //超时之后保存值,但是并不使用
                dataList = testList
                if (!atomicReturned.getAndSet(true)) {
                    launch(Dispatchers.Main) {
                        action.invoke(testList)
                    }
                }
            }
        }
    }

    private fun obtainLocal(): ArrayList<ServerEntity> {
        //logger("读取本地：$data")
        return try {
            parseList(JSONObject(String(Base64.decode(DEFAULT_SERVERS, Base64.NO_WRAP))))
        } catch (e: Exception) {
            ArrayList()
        }
    }

    fun startCheck(result: (ipEntity: ServerEntity?) -> Unit) = GlobalScope.launch {
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
                result(list[Random().nextInt(list.size)])
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
                val list = parseList(jb)
                result(list)

                /*curState = jb.optInt("cur_state", 0)
                val serverArr = jb.getJSONArray("list")
                for (i in 0 until serverArr.length()) {
                    val ipJb = serverArr.optJSONObject(i)
                    val ip = ipJb.optString("ip")
                    val port = ipJb.optInt("port")
                    val pwd = ipJb.optString("pwd")
                    val code = ipJb.optString("code")
                    val country = ipJb.optString("country")
                    val name = ipJb.optString("city")
                    val ipEntity = ServerEntity(
                        isFaster = false,
                        code = code,
                        country = country,
                        name = name,
                        host = ip,
                        port = port,
                        pwd = pwd)
                    list.add(ipEntity)
                }*/
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun parseList(json: JSONObject): ArrayList<ServerEntity> {
        curState = json.optInt("cur_state", 0)
        val serverArr = json.getJSONArray("list")
        val list = ArrayList<ServerEntity>()
        for (i in 0 until serverArr.length()) {
            val ipJb = serverArr.optJSONObject(i)
            val ip = ipJb.optString("ip")
            val port = ipJb.optInt("port")
            val pwd = ipJb.optString("pwd")
            val code = ipJb.optString("code")
            val country = ipJb.optString("country")
            val name = ipJb.optString("city")
            val ipEntity = ServerEntity(
                isFaster = false,
                code = code,
                country = country,
                name = name,
                host = ip,
                port = port,
                pwd = pwd)
            list.add(ipEntity)
        }

        return list
    }
}