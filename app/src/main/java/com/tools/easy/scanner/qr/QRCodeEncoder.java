package com.tools.easy.scanner.qr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Display;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class does the work of decoding the user's request and extracting all
 * the data to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class QRCodeEncoder {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private Context context;
    private QREncode.Builder encodeBuild;

    QRCodeEncoder(QREncode.Builder build, Context context) {
        this.context = context;
        this.encodeBuild = build;
        if (encodeBuild.getColor() == 0) encodeBuild.setColor(BLACK);

        // This assumes the view is full screen, which is a good assumption
        if (encodeBuild.getSize() == 0) {
            int smallerDimension = getSmallerDimension(context.getApplicationContext());
            encodeBuild.setSize(smallerDimension);
        }
        Bitmap logoBitmap = encodeBuild.getLogoBitmap();
        if (logoBitmap != null) {
            int logoSize = Math.min(logoBitmap.getWidth(), logoBitmap.getHeight()) / 2;
            if (encodeBuild.getLogoSize() > 0 && encodeBuild.getLogoSize() < logoSize) {
                logoSize = encodeBuild.getLogoSize();
            }
            encodeBuild.setLogoBitmap(logoBitmap, logoSize);
        }
        encodeContentsFromZXing(build);
    }

    private static List<String> getAllBundleValues(Bundle bundle, String[] keys) {
        List<String> values = new ArrayList<>(keys.length);
        for (String key : keys) {
            Object value = bundle.get(key);
            values.add(value == null ? null : value.toString());
        }
        return values;
    }

    private static int getSmallerDimension(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        int width = displaySize.x;
        int height = displaySize.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 7 / 8;
        return smallerDimension;
    }

    private static Bitmap addBackground(Bitmap qrBitmap, Bitmap background) {
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int fgWidth = qrBitmap.getWidth();
        int fgHeight = qrBitmap.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(background, 0, 0, null);
        //???????????????????????????
        float left = (bgWidth - fgWidth) / 2;
        float top = (bgHeight - fgHeight) / 2;
        canvas.drawBitmap(qrBitmap, left, top, null);
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    Bitmap encodeAsBitmap() throws WriterException {
        String content = encodeBuild.getEncodeContents();
        BarcodeFormat barcodeFormat = encodeBuild.getBarcodeFormat();
        int qrColor = encodeBuild.getColor();
        int size = encodeBuild.getSize();
        Bitmap logoBitmap = encodeBuild.getLogoBitmap();
        if (logoBitmap != null)
            return encodeAsBitmap(content, barcodeFormat, qrColor, size, logoBitmap, encodeBuild.getLogoSize());
        return encodeAsBitmap(content, barcodeFormat, qrColor, size);
    }

    private void encodeContentsFromZXing(QREncode.Builder build) {
        if (build.getBarcodeFormat() == null || build.getBarcodeFormat() == BarcodeFormat.QR_CODE) {
            build.setBarcodeFormat(BarcodeFormat.QR_CODE);
            encodeQRCodeContents(build);
        }
    }

    private void encodeQRCodeContents(QREncode.Builder build) {
        switch (build.getParsedResultType()) {
            case WIFI:
            case ISBN:
            case CALENDAR:
            case PRODUCT:
            case VIN:
            case URI:
            case TEXT:
            case EMAIL_ADDRESS:
            case TEL:
            case SMS:
                encodeBuild.setEncodeContents(build.getContents());
                break;
            case ADDRESSBOOK:
                Bundle contactBundle = null;
                //uri??????
                Uri addressBookUri = build.getAddressBookUri();
                if (addressBookUri != null)
                    try {
                        contactBundle = new ParserUriToVCard().parserUri(context, addressBookUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                //Bundle??????
                if (contactBundle == null || contactBundle.isEmpty())
                    contactBundle = build.getBundle();
                if (contactBundle != null) {
                    String name = contactBundle.getString(ContactsContract.Intents.Insert.NAME);
                    String organization = contactBundle.getString(ContactsContract.Intents.Insert.COMPANY);
                    String address = contactBundle.getString(ContactsContract.Intents.Insert.POSTAL);
                    List<String> phones = getAllBundleValues(contactBundle, ParserUriToVCard.PHONE_KEYS);
                    List<String> phoneTypes = getAllBundleValues(contactBundle, ParserUriToVCard.PHONE_TYPE_KEYS);
                    List<String> emails = getAllBundleValues(contactBundle, ParserUriToVCard.EMAIL_KEYS);
                    String url = contactBundle.getString(ParserUriToVCard.URL_KEY);
                    List<String> urls = url == null ? null : Collections.singletonList(url);
                    String note = contactBundle.getString(ParserUriToVCard.NOTE_KEY);
                    ContactEncoder encoder = build.isUseVCard() ?
                            new VCardContactEncoder() : new MECARDContactEncoder();
                    String[] encoded = encoder.encode(Collections.singletonList(name), organization
                            , Collections.singletonList(address), phones, phoneTypes, emails, urls, note);
                    // Make sure we've encoded at least one field.
                    if (!encoded[1].isEmpty()) {
                        encodeBuild.setEncodeContents(encoded[0]);
                    }
                }
                break;
            case GEO:
                Bundle locationBundle = build.getBundle();
                if (locationBundle != null) {
                    float latitude = locationBundle.getFloat("LAT", Float.MAX_VALUE);
                    float longitude = locationBundle.getFloat("LONG", Float.MAX_VALUE);
                    if (latitude != Float.MAX_VALUE && longitude != Float.MAX_VALUE) {
                        encodeBuild.setEncodeContents("geo:" + latitude + ',' + longitude);
                    }
                }
                break;
        }
    }

    private Bitmap encodeAsBitmap(String content, BarcodeFormat barcodeFormat, int qrColor, int size)
            throws WriterException {
        if (content == null) {
            return null;
        }

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, Charset.forName("UTF-8").name());
        hints.put(EncodeHintType.MARGIN, encodeBuild.getMargin());
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(content, barcodeFormat, size, size, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                // ?????????????????????
                if (result.get(x, y)) {
                    int[] colors = encodeBuild.getColors();
                    if (colors != null) {
                        if (x < size / 2 && y < size / 2) {
                            pixels[y * size + x] = colors[0];// ??????
                        } else if (x < size / 2 && y > size / 2) {
                            pixels[y * size + x] = colors[1];// ??????
                        } else if (x > size / 2 && y > size / 2) {
                            pixels[y * size + x] = colors[2];// ??????
                        } else {
                            pixels[y * size + x] = colors[3];// ??????
                        }
                    } else {
                        pixels[offset + x] = qrColor;
                    }
                } else {
                    int qrBgColor = encodeBuild.getQrBackgroundColor();
                    pixels[offset + x] = qrBgColor == 0 ? WHITE : qrBgColor;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private Bitmap encodeAsBitmap(String content, BarcodeFormat barcodeFormat, int qrColor, int size
            , Bitmap logoBitmap, int logoSize) throws WriterException {
        if (content == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, Charset.forName("UTF-8").name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);// ?????????
        hints.put(EncodeHintType.MARGIN, encodeBuild.getMargin()); // default is 4

        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(content, barcodeFormat, size, size, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                if (x > halfW - logoSize && x < halfW + logoSize && y > halfH - logoSize && y < halfH + logoSize) {
                    pixels[y * width + x] = logoBitmap.getPixel(x - halfW + logoSize, y - halfH + logoSize);
                } else {
                    // ?????????????????????
                    if (result.get(x, y)) {
                        int[] colors = encodeBuild.getColors();
                        if (colors != null) {
                            if (x < size / 2 && y < size / 2) {
                                pixels[y * size + x] = colors[0];// ??????
                            } else if (x < size / 2 && y > size / 2) {
                                pixels[y * size + x] = colors[1];// ??????
                            } else if (x > size / 2 && y > size / 2) {
                                pixels[y * size + x] = colors[2];// ??????
                            } else {
                                pixels[y * size + x] = colors[3];// ??????
                            }
                        } else {
                            pixels[offset + x] = qrColor;
                        }
                    } else {
                        int qrBgColor = encodeBuild.getQrBackgroundColor();
                        pixels[offset + x] = qrBgColor == 0 ? WHITE : qrBgColor;
                    }
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        if (encodeBuild.getQrBackground() != null) {
            return addBackground(bitmap, encodeBuild.getQrBackground());
        }
        return bitmap;
    }
}
