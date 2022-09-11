package com.tools.easy.scanner.ui.generate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.tools.easy.scanner.App
import com.tools.easy.scanner.R
import com.tools.easy.scanner.advertise.AdConst
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.advertise.base.AdmobNative
import com.tools.easy.scanner.advertise.base.AdsListener
import com.tools.easy.scanner.advertise.base.BaseAd
import com.tools.easy.scanner.advertise.base.BaseNative
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityCreateBinding
import com.tools.easy.scanner.support.CreateEntity
import com.tools.easy.scanner.support.Supports
import java.lang.StringBuilder

/**
 *  description :
 */
class CreateActivity: BasicActivity<ActivityCreateBinding>() {

    override fun vBinding(): ActivityCreateBinding {
        return ActivityCreateBinding.inflate(layoutInflater)
    }

    private var catType: String = ""

    companion object {
        fun openCreatePage(context: Context, type: String) {
            val intent = Intent(context, CreateActivity::class.java)
            intent.putExtra("type", type)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdLoader.preloadAd(AdConst.adResult)
        catType = intent.getStringExtra("type") ?: ""
        loadPage(catType)
        
        binding.tvGenerate.setOnClickListener {
            doGenerate()
        }
        binding.back.setOnClickListener { finish() }
        showAd()
    }
    
    private fun doGenerate() {
        ResultCreateActivity.openCreateResultPage(
            this, CreateEntity(
                catType,
                when (catType) {
                    //社交网站
                    Supports.catInstagram,
                    Supports.catFacebook,
                    Supports.catTwitter,
                    Supports.catYoutube,
                    Supports.catTiktok,
                    Supports.catWhatsapp -> formatSocialUrl(
                        catType,
                        binding.etInputName.text.toString()
                    )

                    Supports.catWebsite,
                    Supports.catClipboard,
                    Supports.catText,
                    Supports.catWebsite -> formatContents(
                        catType,
                        binding.etInputTxt.text.toString()
                    )

                    else -> ""
                }
            )
        )
        finish()
    }

    private fun formatContents(categoryType: String, vararg contents: String): String {
        if (contents.isEmpty()) return ""
        val builder = StringBuilder()

        //换行拼接输入信息
        fun appendContents() {
            for (i in contents.indices) {
                if (builder.isNotEmpty()) {
                    builder.append("\n")
                }
                builder.append(contents[i])
            }
        }
        /*
        * 拼接Email
        * @see EmailAddressResultParser
        * mailto:to=xxx@xxx.com?body=xxx&subject=xxx&...
        * */
        /*fun appendEmail() {
            val address = contents[0]
            if (!address.contains("@")) return
            builder.append("mailto:").append(contents[0])
            if (contents.size > 1) {
                builder.append("?")
                    .append("body=")
                    .append(contents[1])
            }
        }*/

        appendContents()
        return builder.toString()
    }

    private fun formatSocialUrl(categoryType: String, userName: String): String {
        val prefix = when (categoryType) {
            Supports.catFacebook -> Supports.prefixFacebook
            Supports.catInstagram -> Supports.prefixInstagram
            Supports.catTwitter -> Supports.prefixTwitter
            Supports.catTiktok -> Supports.prefixTikTok
            Supports.catWhatsapp -> Supports.prefixWhatsApp
            Supports.catYoutube -> Supports.prefixYoutube
            else -> return userName
        }
        return prefix + userName
    }

    fun loadPage(catType: String) {
        when (catType) {
            //单行文本
            Supports.catClipboard -> loadSingleLine()
            Supports.catText -> loadSingleLine()
            Supports.catWebsite -> loadSingleLine()

            //用户名系列
            Supports.catEmail -> loadUserName()
            Supports.catInstagram -> loadUserName()
            Supports.catFacebook -> loadUserName()
            Supports.catTwitter -> loadUserName()
            Supports.catYoutube -> loadUserName()
            Supports.catTiktok -> loadUserName()
            Supports.catWhatsapp -> loadUserName()
            //特殊处理
            //Supports.catWifi -> viewCallback { loadWifiPage() }
            //Supports.catContacts -> viewCallback { loadTwoLinesTxtPage() }
        }
    }

    private fun loadSingleLine() {
        binding.apply {
            etInputTxt.visibility = View.VISIBLE
            groupUser.visibility = View.GONE
            when (catType) {
                Supports.catClipboard -> {
                    createType.setImageResource(R.mipmap.ic_home_clipboard)
                    createName.setImageResource(R.mipmap.hm_clipboard)
                }

                Supports.catText -> {
                    createType.setImageResource(R.mipmap.ic_home_text)
                    createName.setImageResource(R.mipmap.hm_text)
                }

                Supports.catWebsite -> {
                    createType.setImageResource(R.mipmap.ic_home_website)
                    createName.setImageResource(R.mipmap.hm_websites)
                }

                else -> return
            }

            etInputTxt.requestFocus()
        }
    }

    private fun loadUserName() {
        binding.apply {
            etInputTxt.visibility = View.GONE
            groupUser.visibility = View.VISIBLE
            when (catType) {
                Supports.catEmail -> {
                    createType.setImageResource(R.mipmap.ic_home_email)
                    createName.setImageResource(R.mipmap.hm_email)
                }

                Supports.catInstagram -> {
                    createType.setImageResource(R.mipmap.ic_home_instagram)
                    createName.setImageResource(R.mipmap.hm_instagram)
                }

                Supports.catFacebook -> {
                    createType.setImageResource(R.mipmap.ic_home_facebook)
                    createName.setImageResource(R.mipmap.hm_facebook)
                }

                Supports.catTwitter -> {
                    createType.setImageResource(R.mipmap.ic_home_twitter)
                    createName.setImageResource(R.mipmap.hm_twitter)
                }

                Supports.catYoutube -> {
                    createType.setImageResource(R.mipmap.ic_home_youtube)
                    createName.setImageResource(R.mipmap.hm_youtube)
                }

                Supports.catTiktok -> {
                    createType.setImageResource(R.mipmap.ic_home_tiktok)
                    createName.setImageResource(R.mipmap.hm_tiktok)
                }

                Supports.catWhatsapp -> {
                    createType.setImageResource(R.mipmap.ic_home_whatsapp)
                    createName.setImageResource(R.mipmap.hm_whatsapp)
                }

                else -> return
            }

            etInputName.requestFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAd?.onDestroy()
    }

    private var nativeAd: BaseNative? = null
    private fun showAd() {
        AdLoader.loadAd(App.ins, AdConst.adResult, object : AdsListener(){
            override fun onAdLoaded(ad: BaseAd) {
                when (ad) {
                    is BaseNative -> {
                        nativeAd?.onDestroy()
                        nativeAd = ad
                        onNativeLoaded(ad)
                    }

                    else -> return
                }
            }

            override fun onAdError(err: String?) {
                onNativeLoaded(null)
            }
        })
    }

    private fun onNativeLoaded(ad: BaseNative?) {
        if (ad !is AdmobNative) {
            binding.adHold.visibility = View.VISIBLE
            val adContainer = binding.adMain.root
            adContainer.visibility = View.GONE
            return
        }

        binding.adHold.visibility = View.GONE
        binding.adMain.root.apply {
            visibility = View.VISIBLE

            ad.showIcon(this, findViewById(R.id.ad_icon))
            ad.showImage(this, findViewById(R.id.ad_img))
            ad.showTitle(this, findViewById(R.id.ad_title))
            ad.showBody(this, findViewById(R.id.ad_desc))
            ad.showCta(this, findViewById(R.id.ad_action))
            ad.register(this)
        }
    }
}