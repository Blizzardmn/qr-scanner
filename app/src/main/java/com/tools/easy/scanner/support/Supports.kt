package com.tools.easy.scanner.support

import android.content.Context
import com.google.zxing.client.result.ParsedResultType
import com.tools.easy.scanner.R

/**
 *  description :
 */
object Supports {

    const val isDebug = false

    //Twitter 主页
    const val TwitterPrefix = "https://twitter.com/home?user="

    //Instagram 主页
    const val InstagramPrefix = "http://instagram.com/"

    //Facebook 主页
    const val FacebookPrefix = "https://www.facebook.com/"

    //TikTok 个人主页 eg: https://www.tiktok.com/@user27790835
    const val TikTokPrefix = "https://www.tiktok.com/@"

    //Youtube 个人主页
    const val YoutubePrefix = "https://www.youtube.com/?"

    //whatsapp 个人主页
    const val WhatsAppPrefix = "https://www.whatsapp.com/?user="

    /*fun getItemType(type: ParsedResultType): Pair<String, Int> {
        var scanType = ""
        var iconRes = R.mipmap.ic_text

        when (type) {
            ParsedResultType.ADDRESSBOOK -> {
                scanType = ScanItem.ADDRESS_BOOK
                iconRes = R.mipmap.ic_contact
            }

            ParsedResultType.EMAIL_ADDRESS -> {
                scanType = ScanItem.EMAIL

                iconRes = R.mipmap.ic_email
            }

            ParsedResultType.PRODUCT -> {
                scanType = ScanItem.PRODUCT

                iconRes = R.mipmap.ic_prodcut
            }

            ParsedResultType.URI -> {
                scanType = ScanItem.URI

                iconRes = R.mipmap.ic_website
            }

            ParsedResultType.GEO -> {
                scanType = ScanItem.GEO

                iconRes = R.mipmap.ic_geo
            }

            ParsedResultType.TEL -> {
                scanType = ScanItem.TEL

                iconRes = R.mipmap.ic_call
            }

            ParsedResultType.SMS -> {
                scanType = ScanItem.SMS

                iconRes = R.mipmap.ic_sms
            }

            ParsedResultType.WIFI -> {
                scanType = ScanItem.WIFI

                iconRes = R.mipmap.ic_wifi
            }

            ParsedResultType.ISBN -> {
                scanType = ScanItem.ISBN
            }

            ParsedResultType.VIN -> {
                scanType = ScanItem.VIN
            }

            ParsedResultType.CALENDAR -> {
                scanType = ScanItem.CALENDAR
            }

            ParsedResultType.TEXT -> {
                scanType = ScanItem.TEXT
                //下个版本细分内容识别
            }

            else -> {
                scanType = ScanItem.TEXT
            }
        }

        return Pair(scanType, iconRes)
    }

    fun checkSns(context: Context, content: String): SnsItem? {
        return when {
            content.split(FacebookPrefix).size > 1 -> {
                SnsItem(
                    R.mipmap.ic_fb,
                    context.getString(R.string.fb),
                    content.split(FacebookPrefix)[1]
                )
            }
            content.split(InstagramPrefix).size > 1 -> {
                SnsItem(
                    R.mipmap.ic_ins,
                    context.getString(R.string.ins),
                    content.split(InstagramPrefix)[1]
                )
            }
            content.split(TikTokPrefix).size > 1 -> {
                SnsItem(
                    R.mipmap.ic_tt,
                    context.getString(R.string.tiktok),
                    content.split(TikTokPrefix)[1]
                )
            }
            content.split(TwitterPrefix).size > 1 -> {
                SnsItem(
                    R.mipmap.ic_twitter,
                    context.getString(R.string.twitter),
                    content.split(TwitterPrefix)[1]
                )
            }
            content.split(WhatsAppPrefix).size > 1 -> {
                SnsItem(
                    R.mipmap.ic_whatsapp,
                    context.getString(R.string.whatsapp),
                    content.split(WhatsAppPrefix)[1]
                )
            }
            content.split(YoutubePrefix).size > 1 -> {
                SnsItem(
                    R.mipmap.ic_ytb,
                    context.getString(R.string.youtube),
                    content.split(YoutubePrefix)[1]
                )
            }
            else -> null
        }
    }*/

    data class SnsItem(val iconRes: Int, val category: String, val name: String)
}