package com.tools.easy;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

class ProcessDataTask extends AsyncTask<Void, Void, BarcodeResult> {
    private Camera mCamera;
    private byte[] mData;
    private boolean mIsPortrait;
    private Uri mPictureUri;
    private Bitmap mBitmap;
    private WeakReference<QRCodeView> mQRCodeViewRef;
    private static long sLastStartTime = 0;
    private WeakReference<Context> context;

    ProcessDataTask(Camera camera, byte[] data, QRCodeView qrCodeView, boolean isPortrait) {
        mCamera = camera;
        mData = data;
        mQRCodeViewRef = new WeakReference<>(qrCodeView);
        mIsPortrait = isPortrait;
    }

    ProcessDataTask(Context ctx, Uri pictureUri, QRCodeView qrCodeView) {
        mPictureUri = pictureUri;
        mQRCodeViewRef = new WeakReference<>(qrCodeView);
        context = new WeakReference<>(ctx);
    }

    ProcessDataTask(Bitmap bitmap, QRCodeView qrCodeView) {
        mBitmap = bitmap;
        mQRCodeViewRef = new WeakReference<>(qrCodeView);
    }

    ProcessDataTask perform() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return this;
    }

    void cancelTask() {
        if (getStatus() != Status.FINISHED) {
            cancel(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mQRCodeViewRef.clear();
        mBitmap = null;
        mData = null;
    }

    private SourceData getSourceData(QRCodeView qrCodeView) {
        if (mData == null) {
            return null;
        }
        int width = 0;
        int height = 0;
        byte[] data = mData;
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            int format = parameters.getPreviewFormat();
            width = size.width;
            height = size.height;

            if (mIsPortrait) {
                data = new byte[mData.length];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        data[x * height + height - y - 1] = mData[x + y * width];
                    }
                }
                int tmp = width;
                width = height;
                height = tmp;
            }

            return new SourceData(data, width, height, format, 0);
        } catch (Exception e) {
            return null;
        }
    }

    private BarcodeResult processData(QRCodeView qrCodeView) {
        if (mData == null) {
            return null;
        }

        int width = 0;
        int height = 0;
        byte[] data = mData;
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            int format = parameters.getPreviewFormat();
            width = size.width;
            height = size.height;

            if (mIsPortrait) {
                data = new byte[mData.length];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        data[x * height + height - y - 1] = mData[x + y * width];
                    }
                }
                int tmp = width;
                width = height;
                height = tmp;
            }

            return qrCodeView.processData(data, width, height, format,false);
        } catch (Exception e1) {
            e1.printStackTrace();
            try {
                if (width != 0 && height != 0) {
                    QRCodeUtils.d("识别失败重试");
                    return qrCodeView.processData(data, width, height, mCamera.getParameters().getPreviewFormat(),true);
                } else {
                    return null;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }

    @Override
    protected BarcodeResult doInBackground(Void... params) {
        QRCodeView qrCodeView = mQRCodeViewRef.get();
        if (qrCodeView == null) {
            return null;
        }

        if (mPictureUri != null) {
            Bitmap bitmap = null;
            Context ctx = this.context.get();
            try {
                bitmap = BitmapUtils.getBitmapFormUri(ctx, mPictureUri, 500.0f, 500.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return qrCodeView.processBitmapData(bitmap);
        } else if (mBitmap != null) {
            BarcodeResult result = qrCodeView.processBitmapData(mBitmap);
            mBitmap = null;
            return result;
        } else {
            if (QRCodeUtils.isDebug()) {
                QRCodeUtils.d("两次任务执行的时间间隔：" + (System.currentTimeMillis() - sLastStartTime));
                sLastStartTime = System.currentTimeMillis();
            }
            long startTime = System.currentTimeMillis();

            BarcodeResult scanResult = processData(qrCodeView);

            if (QRCodeUtils.isDebug()) {
                long time = System.currentTimeMillis() - startTime;
                if (scanResult != null && !TextUtils.isEmpty(scanResult.getResult().getText())) {
                    QRCodeUtils.d("识别成功时间为：" + time);
                } else {
                    QRCodeUtils.e("识别失败时间为：" + time);
                }
            }

            return scanResult;
        }
    }

    @Override
    protected void onPostExecute(BarcodeResult result) {
        QRCodeView qrCodeView = mQRCodeViewRef.get();
        if (qrCodeView == null) {
            return;
        }

        if (mPictureUri != null || mBitmap != null) {
            mBitmap = null;
            qrCodeView.onPostParseBitmapOrPicture(result);
        } else {
            qrCodeView.onPostParseData(result);
        }
    }
}
