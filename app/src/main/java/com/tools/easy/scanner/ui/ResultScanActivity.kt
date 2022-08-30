package com.tools.easy.scanner.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import androidx.annotation.NonNull
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.zxing.client.result.*
import com.tools.easy.scanner.R
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityResultBinding
import com.tools.easy.scanner.support.GpSupport
import com.tools.easy.scanner.support.Supports
import java.util.*

/**
 *  description :
 */
class ResultScanActivity: BasicActivity<ActivityResultBinding>() {

    override fun vBinding(): ActivityResultBinding {
        return ActivityResultBinding.inflate(layoutInflater)
    }

    companion object {
        private var parsedResult: ParsedResult? = null

        fun open(
            @NonNull activity: Activity,
            @NonNull parseResult: ParsedResult
        ) {
            parsedResult = parseResult
            val intent = Intent(activity, ResultScanActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.back.setOnClickListener { finish() }
        initResult()
    }

    private fun initCopy(content: String) {
        binding.tvCopy.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Label", content)
            clipboard.setPrimaryClip(clip)
            toastLong("Copied to clipboard")
        }
    }

    private fun initBrowser(content: String) {
        binding.tvBrowser.setOnClickListener {
            if (Patterns.WEB_URL.matcher(content).matches() || URLUtil.isValidUrl(content)) {
                GpSupport.openUrlByBrowser(content)
            } else {
                GpSupport.openUrlByBrowser("https://www.google.com/search?q=$content")
            }
        }
    }

    private fun initShare(title: String, content: String) {
        binding.tvShare.setOnClickListener {
            GpSupport.share(this, title, content)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initResult() {
        val content = parsedResult?.displayResult ?: ""

        /**
         * @param category 类型
         * @param str 要分享或copy的内容
         */
        fun initClick(category: String, str: String?) {
            binding.category.text = category
            if (str.isNullOrEmpty()) return
            initCopy(str)
            initBrowser(str)
            initShare(category, str)
        }

        var iconRes = R.mipmap.ic_home_text
        var typeName = parsedResult?.type?.name

        when (parsedResult?.type) {

            ParsedResultType.EMAIL_ADDRESS -> {
                iconRes = R.mipmap.ic_home_email
                val category = Supports.catEmail
                val emailResult = parsedResult as EmailAddressParsedResult
                binding.content2.visibility = View.VISIBLE
                binding.content3.visibility = View.VISIBLE
                binding.content1.text = String.format("To: %s", Arrays.toString(emailResult.tos))
                binding.content2.text = emailResult.subject
                binding.content3.text = emailResult.body

                initClick(category, content)
                /*img_email.visibility = View.VISIBLE
                img_email.setOnClickListener {
                    val destEmail = if (emailResult.tos.isNullOrEmpty()) {
                        ""
                    } else {
                        emailResult.tos[0].trim()
                    }
                    val subject = emailResult.subject ?: ""
                    val body = emailResult.body ?: ""

                    ActionUtils.sendEmail(this, destEmail, subject, body)
                }*/
            }

            ParsedResultType.PRODUCT -> {
                iconRes = R.mipmap.ic_home_text
                val productResult = parsedResult as ProductParsedResult
                val category = Supports.catProduct
                binding.content1.text = productResult.productID ?: ""
                initClick(category, productResult.productID)
            }

            ParsedResultType.URI -> {
                val urlResult = parsedResult as URIParsedResult

                val itemUri = Supports.checkUri(this, content)
                val category = if (itemUri != null) {
                    iconRes = itemUri.iconRes
                    binding.content1.text = "Username: ${itemUri.userName}"
                    typeName = itemUri.userName
                    itemUri.category
                } else {
                    iconRes = R.mipmap.ic_home_website
                    binding.content3.visibility = View.VISIBLE
                    binding.content1.text = urlResult.title
                    binding.content3.text = urlResult.uri
                    Supports.catWebsite
                }

                initClick(category, content)
            }

            /*ParsedResultType.GEO -> {
                iconRes = R.mipmap.ic_geo
                val df = DecimalFormat("0.000000")
                val geoResult = parsedResult as GeoParsedResult
                val category = getString(R.string.geo)
                binding.content1.text = geoResult.query
                binding.content3.visibility = View.VISIBLE
                binding.content3.text = getString(
                    R.string.geo_desc,
                    df.format(geoResult.latitude),
                    df.format(geoResult.longitude),
                    df.format(geoResult.altitude)
                )

                initClick(
                    category,
                    "${df.format(geoResult.latitude)},${df.format(geoResult.longitude)}"
                )
            }*/

            /*ParsedResultType.TEL -> {
                iconRes = R.mipmap.ic_call
                val telResult = parsedResult as TelParsedResult
                val category = getString(R.string.tel)
                val number = telResult.number ?: ""
                binding.content1.text = number
                initClick(category, number)
                img_dial.visibility = View.VISIBLE
                img_dial.setOnClickListener {
                    //拨打电话
                    if (!EasyPermissions.hasPermissions(this, Manifest.permission.CALL_PHONE)) {
                        EasyPermissions.requestPermissions(
                            this,
                            "",
                            REQUEST_CALL_CODE,
                            Manifest.permission.CALL_PHONE
                        )
                    } else {
                        ActionUtils.callPhone(this, number)
                    }
                }
            }*/

            /*ParsedResultType.SMS -> {
                iconRes = R.mipmap.ic_sms
                val smsResult = parsedResult as SMSParsedResult
                val category = getString(R.string.sms)
                val body = smsResult.body ?: ""
                binding.content1.text = body
                initClick(category, body)
                //发送短信要权限，酌情考虑
            }*/

            /*ParsedResultType.WIFI -> {
                iconRes = R.mipmap.ic_wifi
                val wifiResult = parsedResult as WifiParsedResult
                val ssid = wifiResult.ssid ?: ""
                val encryption = wifiResult.networkEncryption ?: ""
                val pwd = wifiResult.password ?: ""
                val category = getString(R.string.wifi)
                initClick(category, pwd)
                binding.content2.visibility = View.VISIBLE
                binding.content3.visibility = View.VISIBLE
                binding.content1.text = getString(R.string.name, ssid)
                binding.content2.text = getString(R.string.encryption, encryption)
                binding.content3.text = getString(R.string.password, pwd)
            }*/

            ParsedResultType.ISBN -> {
                val category = "ISBN"
                binding.content1.text = content
                initClick(category, content)
            }

            ParsedResultType.VIN -> {
                val category = "VIN"
                binding.content1.text = content
                initClick(category, content)
            }

            ParsedResultType.CALENDAR -> {
                val category = "Calendar"
                binding.content1.text = content
                initClick(category, content)
            }

            ParsedResultType.TEXT -> {
                val category = Supports.catText

                binding.content1.text = content
                initClick(category, content)
            }

            else -> {
                val category = Supports.catText
                binding.content1.text = content
                initClick(category, content)
            }
        }

        Glide.with(this)
            .load(iconRes)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.imgCat)
    }
}