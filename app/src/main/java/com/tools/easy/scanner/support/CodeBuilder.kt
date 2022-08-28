package com.tools.easy.scanner.support

import android.content.Context
import android.graphics.Color
import com.google.zxing.client.result.ParsedResultType
import com.tools.easy.scanner.qr.QREncode

/**
 *  description :
 */
object CodeBuilder {

    const val qrSize = 440

    /**
     * 生成二维码
     * @param singleTxt 二维码字符串内容.单行文本
     * @param expectSize 期望大小,等宽高,不穿默认450
     * */
    fun buildQRCode(context: Context,
                    singleTxt: String,
                    expectSize: Int = qrSize,
                    parsedType: ParsedResultType = ParsedResultType.TEXT): QREncode {
        return buildQRCode(context, arrayOf(singleTxt), expectSize, parsedType)
    }

    /**
     * 生成二维码
     * @param contents 二维码字符串内容,多行文本
     * @param expectSize 期望大小,等宽高,不穿默认450
     * */
    fun buildQRCode(context: Context,
                    contents: Array<String>?,
                    expectSize: Int = qrSize,
                    parsedType: ParsedResultType = ParsedResultType.TEXT): QREncode {
        val builder = QREncode.Builder(context)
        return builder.setColor(Color.parseColor("#121212")) //二维码颜色
            .setMargin(2) //二维码边框
            .setSize(expectSize)
            .setParsedResultType(parsedType)
            .setContents(contents)
            .build()
    }

}