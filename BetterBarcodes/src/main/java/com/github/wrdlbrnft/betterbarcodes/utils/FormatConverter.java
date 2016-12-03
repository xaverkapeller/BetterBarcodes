package com.github.wrdlbrnft.betterbarcodes.utils;

import android.util.SparseArray;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 11/04/16
 */
public class FormatConverter {

    private static final SparseArray<com.google.zxing.BarcodeFormat> FORMAT_MAP = new SparseArrayBuilder<com.google.zxing.BarcodeFormat>()
            .put(BarcodeFormat.AZTEC, com.google.zxing.BarcodeFormat.AZTEC)
            .put(BarcodeFormat.CODABAR, com.google.zxing.BarcodeFormat.CODABAR)
            .put(BarcodeFormat.CODE_39, com.google.zxing.BarcodeFormat.CODE_39)
            .put(BarcodeFormat.CODE_93, com.google.zxing.BarcodeFormat.CODE_93)
            .put(BarcodeFormat.CODE_128, com.google.zxing.BarcodeFormat.CODE_128)
            .put(BarcodeFormat.DATA_MATRIX, com.google.zxing.BarcodeFormat.DATA_MATRIX)
            .put(BarcodeFormat.EAN_8, com.google.zxing.BarcodeFormat.EAN_8)
            .put(BarcodeFormat.EAN_13, com.google.zxing.BarcodeFormat.EAN_13)
            .put(BarcodeFormat.ITF, com.google.zxing.BarcodeFormat.ITF)
            .put(BarcodeFormat.MAXICODE, com.google.zxing.BarcodeFormat.MAXICODE)
            .put(BarcodeFormat.QR_CODE, com.google.zxing.BarcodeFormat.QR_CODE)
            .put(BarcodeFormat.RSS_14, com.google.zxing.BarcodeFormat.RSS_14)
            .put(BarcodeFormat.RSS_EXPANDED, com.google.zxing.BarcodeFormat.RSS_EXPANDED)
            .put(BarcodeFormat.UPC_A, com.google.zxing.BarcodeFormat.UPC_A)
            .put(BarcodeFormat.UPC_E, com.google.zxing.BarcodeFormat.UPC_E)
            .put(BarcodeFormat.UPC_EAN_EXTENSION, com.google.zxing.BarcodeFormat.UPC_EAN_EXTENSION)
            .build();

    public static com.google.zxing.BarcodeFormat toZXing(@BarcodeFormat int format) {
        return FORMAT_MAP.get(format);
    }
}
