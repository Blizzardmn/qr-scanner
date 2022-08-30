package com.tools.easy.scanner.support

import android.content.Context
import com.tools.easy.scanner.App
import com.tools.easy.scanner.R

/**
 *  description :
 */
object Supports {

    const val isRelease = true

    //Twitter 主页
    const val prefixTwitter = "https://twitter.com/home?user="
    //Instagram 主页
    const val prefixInstagram = "http://instagram.com/"
    //Facebook 主页
    const val prefixFacebook = "https://www.facebook.com/"
    //TikTok 个人主页 eg: https://www.tiktok.com/@user27790835
    const val prefixTikTok = "https://www.tiktok.com/@"
    //Youtube 个人主页
    const val prefixYoutube = "https://www.youtube.com/?"
    //whatsapp 个人主页
    const val prefixWhatsApp = "https://www.whatsapp.com/?user="

    val catClipboard: String = App.ins.getString(R.string.cat_clipboard)
    val catEmail: String = App.ins.getString(R.string.cat_email)
    val catWebsite: String = App.ins.getString(R.string.cat_website)
    val catText: String = App.ins.getString(R.string.cat_text)
    val catInstagram: String = App.ins.getString(R.string.cat_instagram)
    val catFacebook: String = App.ins.getString(R.string.cat_facebook)
    val catTwitter: String = App.ins.getString(R.string.cat_twitter)
    val catYoutube: String = App.ins.getString(R.string.cat_youtube)
    val catTiktok: String = App.ins.getString(R.string.cat_tiktok)
    val catWhatsapp: String = App.ins.getString(R.string.cat_whatsapp)
    val catProduct: String = App.ins.getString(R.string.cat_product)
    /*val catCellphone: String = App.ins.getString(R.string.cat_cellphone)
    val catMessage: String = App.ins.getString(R.string.cat_message)
    val catContacts: String = App.ins.getString(R.string.cat_contacts)
    val catWifi: String = App.ins.getString(R.string.cat_wifi)*/

    
    data class ItemUri(val iconRes: Int, val category: String, val userName: String)

    fun checkUri(context: Context, content: String): ItemUri? {
        val splitFacebook = content.split(prefixFacebook)
        val splitIns = content.split(prefixInstagram)
        val splitTik = content.split(prefixTikTok)
        val splitTwitter = content.split(prefixTwitter)
        val splitWhatsapp = content.split(prefixWhatsApp)
        val splitYoutube = content.split(prefixYoutube)

        return when {
            splitFacebook.size > 1 -> {
                ItemUri(
                    R.mipmap.ic_home_facebook,
                    catFacebook,
                    splitFacebook[1]
                )
            }
            splitIns.size > 1 -> {
                ItemUri(
                    R.mipmap.ic_home_instagram,
                    catInstagram,
                    splitIns[1]
                )
            }
            splitTik.size > 1 -> {
                ItemUri(
                    R.mipmap.ic_home_tiktok,
                    catTiktok,
                    splitTik[1]
                )
            }
            splitTwitter.size > 1 -> {
                ItemUri(
                    R.mipmap.ic_home_twitter,
                    catTwitter,
                    splitTwitter[1]
                )
            }
            splitWhatsapp.size > 1 -> {
                ItemUri(
                    R.mipmap.ic_home_whatsapp,
                    catWhatsapp,
                    splitWhatsapp[1]
                )
            }
            splitYoutube.size > 1 -> {
                ItemUri(
                    R.mipmap.ic_home_youtube,
                    catYoutube,
                    splitYoutube[1]
                )
            }

            else -> null
        }
    }
}