package com.tools.easy.scanner.datas.entity

import java.io.Serializable

/**
 *  description :
 */
data class ServerEntity(
    val country: String,
    val name: String,
    val host: String,
    val port: Int,
    val pwd: String
): Serializable