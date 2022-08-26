package com.tools.easy.scanner.support

/**
 *  description :
 */
data class ScanItem(
    val category: String,
    val time: Long,
    val content: String,
    val isGenerate: Boolean,
    val type: Int = 0, //0为记录的扫描/合成结果item，1为日期item
    var isChecked: Boolean = false, //是否勾选
    var parsedContent: String? = null //解析后的展示内容
) {
    companion object {
        const val ADDRESS_BOOK = "address_book"
        const val EMAIL = "email"
        const val PRODUCT = "product"
        const val URI = "uri"
        const val GEO = "geo"
        const val TEL = "tel"
        const val SMS = "sms"
        const val WIFI = "wifi"
        const val ISBN = "isbn"
        const val VIN = "vin"
        const val CALENDAR = "calendar"
        const val TEXT = "text"
        const val INS = "ins"
        const val FB = "fb"
        const val TWITTER = "twitter"
        const val YOUTUBE = "youtube"
        const val WHATSAPP = "whatsapp"
        const val TIKTOK = "tiktok"
    }
}