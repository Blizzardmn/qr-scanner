package com.tools.easy.scanner.support

import com.google.zxing.client.result.ParsedResultType
import java.io.Serializable

/**
 *  description :
 */
data class CreateEntity(val category: String, val body: String): Serializable {


    fun convertStandardType(): ParsedResultType {
        return when (category) {
            Supports.catEmail -> {
                ParsedResultType.EMAIL_ADDRESS
            } /*else if (category == Supports.catContacts) {
                ParsedResultType.ADDRESSBOOK
            } else if (category == Supports.catWifi) {
                ParsedResultType.WIFI
            }*/
            Supports.catInstagram,
            Supports.catFacebook,
            Supports.catTwitter,
            Supports.catYoutube,
            Supports.catTiktok,
            Supports.catWhatsapp,
            Supports.catWebsite -> {
                ParsedResultType.URI
            }/* else if (category == Supports.catProduct) {
                ParsedResultType.PRODUCT
            } else if (category == Supports.catCellphone) {
                ParsedResultType.TEL
            } else if (category == Supports.catMessage) {
                ParsedResultType.SMS
            }*/
            else -> {
                ParsedResultType.TEXT
            }
        }
    }
}