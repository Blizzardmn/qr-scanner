package com.tools.easy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import java.util.Map;

public class ZXingView extends QRCodeView {
    private MultiFormatReader mMultiFormatReader;
    private Map<DecodeHintType, Object> mHintMap;

    public ZXingView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ZXingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void setupReader() {
        mMultiFormatReader = new MultiFormatReader();

        if (mBarcodeType == BarcodeType.ONE_DIMENSION) {
            mMultiFormatReader.setHints(QRCodeDecoder.ONE_DIMENSION_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.TWO_DIMENSION) {
            mMultiFormatReader.setHints(QRCodeDecoder.TWO_DIMENSION_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.ONLY_QR_CODE) {
            mMultiFormatReader.setHints(QRCodeDecoder.QR_CODE_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.ONLY_CODE_128) {
            mMultiFormatReader.setHints(QRCodeDecoder.CODE_128_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.ONLY_EAN_13) {
            mMultiFormatReader.setHints(QRCodeDecoder.EAN_13_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.HIGH_FREQUENCY) {
            mMultiFormatReader.setHints(QRCodeDecoder.HIGH_FREQUENCY_HINT_MAP);
        } else if (mBarcodeType == BarcodeType.CUSTOM) {
            mMultiFormatReader.setHints(mHintMap);
        } else {
            mMultiFormatReader.setHints(QRCodeDecoder.ALL_HINT_MAP);
        }
    }

    /**
     * ?????????????????????
     *
     * @param barcodeType ???????????????
     * @param hintMap     barcodeType ??? BarcodeType.CUSTOM ????????????????????????
     */
    public void setType(BarcodeType barcodeType, Map<DecodeHintType, Object> hintMap) {
        mBarcodeType = barcodeType;
        mHintMap = hintMap;

        if (mBarcodeType == BarcodeType.CUSTOM && (mHintMap == null || mHintMap.isEmpty())) {
            throw new RuntimeException("barcodeType ??? BarcodeType.CUSTOM ??? hintMap ????????????");
        }
        setupReader();
    }

    @Override
    protected BarcodeResult processBitmapData(Bitmap bitmap) {
        return new BarcodeResult(QRCodeDecoder.syncDecodeQRCode(bitmap), bitmap);
    }

    @Override
    protected BarcodeResult processData(byte[] data, int width, int height, int imageFormat, boolean isRetry) {
        Result rawResult = null;
        Rect scanBoxAreaRect = null;

        try {
            PlanarYUVLuminanceSource source;
            scanBoxAreaRect = mScanBoxView.getScanBoxAreaRect(height);
            if (scanBoxAreaRect != null) {
                source = new PlanarYUVLuminanceSource(data, width, height, scanBoxAreaRect.left, scanBoxAreaRect.top, scanBoxAreaRect.width(),
                        scanBoxAreaRect.height(), false);
            } else {
                source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
            }

            rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
            if (rawResult == null) {
                rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
                if (rawResult != null) {
                    QRCodeUtils.d("GlobalHistogramBinarizer ???????????????HybridBinarizer ????????????");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mMultiFormatReader.reset();
        }

        if (rawResult == null) {
            return null;
        }

        SourceData sourceData = new SourceData(data, width, height, imageFormat, 0);
        sourceData.setCropRect(new Rect(0, 0, width, height));
        BarcodeResult barResult = new BarcodeResult(rawResult, sourceData);

        String result = rawResult.getText();
        if (TextUtils.isEmpty(result)) {
            return null;
        }

        BarcodeFormat barcodeFormat = rawResult.getBarcodeFormat();
        QRCodeUtils.d("????????????" + barcodeFormat.name());

        // ??????????????????????????????
        boolean isNeedAutoZoom = isNeedAutoZoom(barcodeFormat);
        if (isShowLocationPoint() || isNeedAutoZoom) {
            ResultPoint[] resultPoints = rawResult.getResultPoints();
            final PointF[] pointArr = new PointF[resultPoints.length];
            int pointIndex = 0;
            for (ResultPoint resultPoint : resultPoints) {
                pointArr[pointIndex] = new PointF(resultPoint.getX(), resultPoint.getY());
                pointIndex++;
            }

            if (transformToViewCoordinates(pointArr, scanBoxAreaRect, isNeedAutoZoom, barResult)) {
                return null;
            }
        }
        return barResult;
    }

    private boolean isNeedAutoZoom(BarcodeFormat barcodeFormat) {
        return isAutoZoom() && barcodeFormat == BarcodeFormat.QR_CODE;
    }
}