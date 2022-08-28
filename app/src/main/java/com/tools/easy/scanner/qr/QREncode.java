package com.tools.easy.scanner.qr;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.result.ParsedResultType;

/**
 * This class encodes data from an Intent into a QR code, and then displays it
 * full screen so that another person can scan it with their device.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QREncode {
    private QRCodeEncoder mQRCodeEncoder;

    private QREncode() {
    }

    private QREncode(QRCodeEncoder codeEncoder) {
        this.mQRCodeEncoder = codeEncoder;
    }

    /**
     * @param codeEncoder {@linkplain Builder#buildDeprecated()} () QREncode.Builder()
     *                    .buildDeprecated()}
     * @return
     */
    @Deprecated
    public static Bitmap encodeQR(QRCodeEncoder codeEncoder) {
        try {
            return codeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@linkplain Builder#build()} () QREncode.Builder().build()}
     *
     * @return
     */
    public Bitmap encodeAsBitmap() {
        try {
            return mQRCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Builder {

        BarcodeFormat barcodeFormat;
        private Context context;
        //创建二维码类型
        private ParsedResultType parsedResultType = ParsedResultType.TEXT;
        private Bundle bundle;
        //原内容
        private String contents;
        //编码内容
        private String encodeContents;
        private int color;//颜色
        private int[] colors;
        //设置联系人Uri
        private Uri addressBookUri;
        private boolean useVCard = true;
        private int size;
        private Bitmap logoBitmap;
        private int logoSize;
        private Bitmap qrBackground;
        private int qrBackgroundColor;
        private int margin = 4;

        public Builder(Context context) {
            this.context = context;
        }

        Builder setBarcodeFormat(BarcodeFormat barcodeFormat) {
            this.barcodeFormat = barcodeFormat;
            return this;
        }
        BarcodeFormat getBarcodeFormat() {
            return barcodeFormat;
        }


        /**
         * 设置二维码类型
         *
         * @param parsedResultType {@linkplain ParsedResultType ParsedResultType}
         * @return
         */
        public Builder setParsedResultType(ParsedResultType parsedResultType) {
            this.parsedResultType = parsedResultType;
            return this;
        }
        ParsedResultType getParsedResultType() {
            return parsedResultType;
        }


        /**
         * 设置联系人Uri
         *
         * @param addressBookUri
         */
        public Builder setAddressBookUri(Uri addressBookUri) {
            this.addressBookUri = addressBookUri;
            return this;
        }
        Uri getAddressBookUri() {
            return addressBookUri;
        }


        /**
         * 设置内容，当 ParsedResultType 是 ADDRESSBOOK 、GEO 类型
         *
         * @param bundle
         * @return
         */
        public Builder setBundle(Bundle bundle) {
            this.bundle = bundle;
            return this;
        }
        Bundle getBundle() {
            return bundle;
        }

        /**
         * 二维码内容
         *
         * @param contents tel、email等不需要前缀
         * @return
         */
        public Builder setContents(String contents) {
            this.contents = contents;
            return this;
        }
        public Builder setContents(String[] contents) {
            if (contents.length < 1) return this;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < contents.length; ++i) {
                builder.append(contents[i]).append("\n");
            }
            this.contents = builder.substring(0, builder.length() - 1);
            return this;
        }
        String getContents() {
            return contents;
        }


        Builder setEncodeContents(String encodeContents) {
            this.encodeContents = encodeContents;
            return this;
        }
        public String getEncodeContents() {
            return encodeContents;
        }


        /**
         * 设置二维码颜色
         *
         * @param color
         * @return
         */
        public Builder setColor(int color) {
            this.color = color;
            return this;
        }
        int getColor() {
            return color;
        }


        /**
         * 设置二维码颜色
         *
         * @param leftTop     左上
         * @param leftBottom  左下
         * @param rightBottom 右下
         * @param rightTop    右上
         * @return
         */
        public Builder setColors(int leftTop, int leftBottom, int rightBottom, int rightTop) {
            this.colors = null;
            this.colors = new int[4];
            this.colors[0] = leftTop;
            this.colors[1] = leftBottom;
            this.colors[2] = rightBottom;
            this.colors[3] = rightTop;
            return this;
        }
        int[] getColors() {
            return colors;
        }


        /**
         * 设置vCard格式，默认true
         *
         * @param useVCard
         * @return
         */
        public Builder setUseVCard(boolean useVCard) {
            this.useVCard = useVCard;
            return this;
        }
        boolean isUseVCard() {
            return useVCard;
        }


        /**
         * 二维码大小
         *
         * @param size
         * @return
         */
        public Builder setSize(int size) {
            this.size = size;
            return this;
        }
        int getSize() {
            return size;
        }

        /**
         * 二维码中间的logo，logoSize不能大于 Math.min(logoBitmap.getWidth(), logoBitmap.getHeight())
         *
         * @param logoBitmap
         * @param logoSize
         * @return
         */
        public Builder setLogoBitmap(Bitmap logoBitmap, int logoSize) {
            this.logoBitmap = logoBitmap;
            this.logoSize = logoSize;
            return this;
        }
        Bitmap getLogoBitmap() {
            return logoBitmap;
        }

        /**
         * 二维码中间的logo
         *
         * @param logoBitmap
         * @return
         */
        public Builder setLogoBitmap(Bitmap logoBitmap) {
            this.logoBitmap = logoBitmap;
            return this;
        }
        int getLogoSize() {
            return logoSize;
        }

        /**
         * 设置二维码背景
         *
         * @param background
         * @return
         */
        public Builder setQrBackground(Bitmap background) {
            this.qrBackground = background;
            return this;
        }

        Bitmap getQrBackground() {
            return qrBackground;
        }

        /**
         * 设置二维码背景
         *
         * @param background
         * @return
         */
        public Builder setQrBackground(int background) {
            this.qrBackgroundColor = background;
            return this;
        }

        int getQrBackgroundColor() {
            return qrBackgroundColor;
        }

        int getMargin() {
            return margin;
        }

        /**
         * 设置二维码边框
         *
         * @param margin 范围值：0-4
         * @return
         */
        public Builder setMargin(int margin) {
            this.margin = margin;
            return this;
        }

        /**
         * @return
         * @deprecated {@link #build()}
         */
        @Deprecated
        public QRCodeEncoder buildDeprecated() {
            checkParams();
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(this, context.getApplicationContext());
            return qrCodeEncoder;
        }

        public QREncode build() {
            checkParams();
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(this, context.getApplicationContext());
            return new QREncode(qrCodeEncoder);
        }

        private void checkParams() {
            if (context == null)
                throw new IllegalArgumentException("context no found...");
            if (parsedResultType == null) {
                throw new IllegalArgumentException("parsedResultType no found...");
            }
            if (parsedResultType != ParsedResultType.ADDRESSBOOK && parsedResultType != ParsedResultType.GEO
                    && contents == null) {
                throw new IllegalArgumentException("parsedResultType not" +
                        " ParsedResultType.ADDRESSBOOK and ParsedResultType.GEO, contents no " +
                        "found...");
            }
            if ((parsedResultType == ParsedResultType.ADDRESSBOOK || parsedResultType == ParsedResultType.GEO)
                    && bundle == null && addressBookUri == null) {
                throw new IllegalArgumentException("parsedResultType yes" +
                        " ParsedResultType.ADDRESSBOOK or ParsedResultType.GEO, bundle and " +
                        "addressBookUri no found...");
            }
        }
    }
}
