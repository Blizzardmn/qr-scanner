package com.tools.easy.scanner.ui.generate

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.google.zxing.client.result.ParsedResultType
import com.tools.easy.scanner.App
import com.tools.easy.scanner.R
import com.tools.easy.scanner.advertise.AdConst
import com.tools.easy.scanner.advertise.AdLoader
import com.tools.easy.scanner.advertise.base.AdmobNative
import com.tools.easy.scanner.advertise.base.AdsListener
import com.tools.easy.scanner.advertise.base.BaseAd
import com.tools.easy.scanner.advertise.base.BaseNative
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityCreateResultBinding
import com.tools.easy.scanner.support.CodeBuilder
import com.tools.easy.scanner.support.CreateEntity
import com.tools.easy.scanner.ui.new.HomeActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 *  description :
 */
class ResultCreateActivity: BasicActivity<ActivityCreateResultBinding>(), EasyPermissions.PermissionCallbacks {

    override fun vBinding(): ActivityCreateResultBinding {
        return ActivityCreateResultBinding.inflate(layoutInflater)
    }

    //只存储一次,避免重复
    private var hasCached = false
    //生成的二维码
    private var mBitmap: Bitmap? = null
    private var createEntity: CreateEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createEntity = intent?.getSerializableExtra("create") as CreateEntity?
        syncLoadParams {
            runOnUiThread {
                binding.imgCode.setImageBitmap(mBitmap)
            }
        }
        binding.back.setOnClickListener { onBackPressed() }
        binding.tvSave.setOnClickListener {
            if (requestStoragePermissions()) {
                doSaveBitmap()
            }
        }
        binding.tvShare.setOnClickListener {
            val bitmap = mBitmap ?: return@setOnClickListener
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            val uri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, "IMG" + Calendar.getInstance().time, null))
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(intent, "title"))
        }
        showAd()
    }

    override fun onBackPressed() {
        HomeActivity.atomicBackAd.set(true)
        super.onBackPressed()
    }

    private fun syncLoadParams(action: () -> Unit) {
        GlobalScope.launch {
            val parseType: ParsedResultType = createEntity?.convertStandardType() ?: return@launch
            mBitmap = CodeBuilder.buildQRCode(
                context = this@ResultCreateActivity,
                contents = arrayOf(createEntity?.body ?: ""),
                parsedType = parseType).encodeAsBitmap()

            action.invoke()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            doSaveBitmap()
        }
    }

    private fun doSaveBitmap() {
        if (!hasCached && addPictureToAlbum()) {
            hasCached = true
            toastLong("Stored Successful")
        }
    }

    private fun addPictureToAlbum(): Boolean {
        if (mBitmap == null) return false
        //文件名为时间
        val timeStamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sd: String = sdf.format(Date(timeStamp))
        val fileName = "qrscanner_$sd.jpg"

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, fileName)
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QRScanner")
        }

        val uri = this.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        var outputStream: OutputStream? = null
        try {
            outputStream = this.contentResolver.openOutputStream(uri!!)
            mBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            outputStream?.close()
        }
        return true
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_STORAGE)
    fun requestStoragePermissions(): Boolean {
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            App.isFilter = true
            EasyPermissions.requestPermissions(
                this@ResultCreateActivity,
                "Permission required for storing",
                REQUEST_PERMISSION_STORAGE,
                *perms
            )
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mBitmap?.recycle()
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

    companion object {
        //请求存储权限
        const val REQUEST_PERMISSION_STORAGE = 1001

        //普通参数型创建二维码
        fun openCreateResultPage(ctx: Context, create: CreateEntity) {
            val intent = Intent(ctx, ResultCreateActivity::class.java)
            intent.putExtra("create", create)
            ctx.startActivity(intent)
        }
    }
}