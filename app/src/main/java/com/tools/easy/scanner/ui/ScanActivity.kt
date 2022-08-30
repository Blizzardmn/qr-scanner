package com.tools.easy.scanner.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.client.result.ResultParser
import com.tools.easy.BarcodeResult
import com.tools.easy.BeepManager
import com.tools.easy.QRCodeUtils
import com.tools.easy.QRCodeView
import com.tools.easy.scanner.App
import com.tools.easy.scanner.BuildConfig
import com.tools.easy.scanner.R
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityScanBinding
import com.tools.easy.scanner.datas.AppConfig
import com.tools.easy.scanner.support.GpSupport
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 *  description :
 */
class ScanActivity: BasicActivity<ActivityScanBinding>(), View.OnClickListener,
    QRCodeView.Delegate, EasyPermissions.PermissionCallbacks {
    
    private var beepManager: BeepManager? = null

    companion object {
        const val reqPermissionQr = 1001
        const val reqPermissionStorage = 1002
        const val reqPermissionReadContact = 1003
        const val reqPermissionVibrate = 1004

        const val PHOTO_REQUEST_GALLERY = 1
    }

    override fun vBinding(): ActivityScanBinding {
        return ActivityScanBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.back.setOnClickListener(this)
        binding.imgPhotos.setOnClickListener(this)
        binding.imgLights.setOnClickListener(this)
        initSettings()
        QRCodeUtils.setDebug(BuildConfig.DEBUG)
        binding.zxingview.setDelegate(this)
    }

    private var isFlashLighting = false
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.back -> finish()

            R.id.img_photos -> {
                if (requestStoragePermissions()) {
                    openGallery()
                }
            }

            R.id.img_lights, R.id.img_light_open -> {
                if (!isFlashLighting) {
                    binding.zxingview.openFlashlight()
                } else {
                    binding.zxingview.closeFlashlight()
                }
                isFlashLighting = !isFlashLighting
                if (isFlashLighting) {
                    binding.imgLightOpen.visibility = View.VISIBLE
                    binding.imgLights.visibility = View.INVISIBLE
                } else {
                    binding.imgLightOpen.visibility = View.INVISIBLE
                    binding.imgLights.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initSettings() {
        beepManager = BeepManager(this)
        val isBeepOn = AppConfig.ins.isScanBeepOn
        beepManager?.isBeepEnabled = isBeepOn
        if (isBeepOn) {
            beepManager?.setBeepVolume(0.2f)
        }
        beepManager?.isVibrateEnabled = AppConfig.ins.isVibrateOn
    }

    override fun onStart() {
        super.onStart()
        requestCodeQRCodePermissions()
        //打开后置摄像头,但是并未开始识别
        binding.zxingview.startCamera()
    }

    override fun onResume() {
        super.onResume()
        //显示扫描框,开始扫描
        binding.zxingview.startSpotAndShowRect()
    }

    override fun onStop() {
        binding.zxingview.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        binding.zxingview.onDestroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onScanQRCodeSuccess(result: BarcodeResult?) {
        if (result == null) return
        if (result.result == null) return
        beepManager?.playBeepSoundAndVibrate()
        val parsedResult = ResultParser.parseResult(result.result)
        if (parsedResult.type == ParsedResultType.URI && AppConfig.ins.isBrowserAuto
            /*&& Supports.checkSns(this, parsedResult.displayResult) == null*/
        ) {
            GpSupport.openUrlByBrowser(parsedResult.displayResult)
        } else {
            try {
                toastLong("ScanResult: $parsedResult")
                ResultScanActivity.open(this, parsedResult)
            } catch (e: Exception) {
            }
        }

        /*GlobalScope.launch {
            ScanDbHelper().insertItem(
                ScanItem(
                    Constant.getItemType(parsedResult.type).first,
                    System.currentTimeMillis(),
                    result.result.text,
                    false
                )
            )
        }*/
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        var tipText = binding.zxingview.scanBoxView.tipText ?: ""
        val ambientBrightnessTip = "\\n It is too dark, please turn on the flash"
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                binding.zxingview.scanBoxView.tipText = tipText + ambientBrightnessTip
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip))
                binding.zxingview.scanBoxView.tipText = tipText
            }
        }
    }

    override fun onScanQRCodeOpenCameraError() {
        toastLong("Opening Camera Error")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            openGallery()
        }
    }

    @AfterPermissionGranted(reqPermissionQr)
    fun requestCodeQRCodePermissions() {
        val perms = arrayOf(
            Manifest.permission.CAMERA
        )
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            App.isFilter = true
            EasyPermissions.requestPermissions(
                this@ScanActivity,
                "Scanning the QR code requires permission to turn on the camera",
                reqPermissionQr,
                *perms
            )
        }
    }

    @AfterPermissionGranted(reqPermissionStorage)
    fun requestStoragePermissions(): Boolean {
        val perms = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            App.isFilter = true
            EasyPermissions.requestPermissions(
                this@ScanActivity,
                "Opening gallery requires album permission",
                reqPermissionStorage,
                *perms
            )
            return false
        }
        return true
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        //开启一个带有返回值的activity,请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var dataUri: Uri? = null
        if (data != null) {
            dataUri = data.data //获取图片uri
            if (dataUri == null) {
                binding.zxingview.startSpotAndShowRect() // 显示扫描框，并开始识别
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
        }

        when (requestCode) {
            //相册返回结果
            PHOTO_REQUEST_GALLERY -> {
                binding.zxingview.decodeQRCode(this, dataUri)
            }

            else -> {
                binding.zxingview.startSpotAndShowRect() // 显示扫描框，并开始识别
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}