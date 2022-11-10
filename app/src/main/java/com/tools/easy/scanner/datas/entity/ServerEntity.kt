package com.tools.easy.scanner.datas.entity

import org.json.JSONObject
import java.io.Serializable

/**
 *  description :
 */
data class ServerEntity(
    var isFaster: Boolean = true,
    val code: String = "",
    val country: String = "",
    val name: String = "",
    val host: String = "",
    val port: Int = 443,
    val pwd: String = ""
): Serializable {

    override fun toString(): String {
        val json = JSONObject()
        json.put("is_f", isFaster)
        json.put("code", code)
        json.put("country", country)
        json.put("name", name)
        json.put("host", host)
        json.put("port", port)
        json.put("pwd", pwd)
        return json.toString()
    }

    companion object {
        fun instance(body: String): ServerEntity {
            if (body.isEmpty()) return ServerEntity()
            return try {
                val js = JSONObject(body)
                ServerEntity(
                    isFaster = js.optBoolean("is_f", true),
                    code = js.optString("code", ""),
                    country = js.optString("country", ""),
                    name = js.optString("name", ""),
                    host = js.optString("host", ""),
                    port = js.optInt("port", 443),
                    pwd = js.optString("pwd", "")
                )
            } catch (e: Exception) {
                ServerEntity()
            }
        }
    }
}