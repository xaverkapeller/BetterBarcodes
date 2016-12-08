package com.github.wrdlbrnft.betterbarcodes.utils;

import android.support.annotation.StringRes;
import android.util.SparseArray;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.R;
import com.github.wrdlbrnft.betterbarcodes.utils.primitives.IntSet;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 11/04/16
 */
public class FormatUtils {

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

    @BarcodeFormat
    public static int[] split(@BarcodeFormat int... formats) {
        final IntSet set = new IntSet();
        for (int format : formats) {
            for (int existingFormat : BarcodeFormat.ALL_FORMATS) {
                final int separatedFormat = format & existingFormat;
                if (separatedFormat > 0) {
                    set.add(separatedFormat);
                }
            }
        }
        //noinspection WrongConstant
        return set.toArray();
    }

    @BarcodeFormat
    public static int combine(@BarcodeFormat int... formats) {
        int result = 0;
        for (int format : formats) {
            result += format;
        }
        return result;
    }

    @StringRes
    public static int getNameForFormat(@BarcodeFormat int format) {
        switch (format) {

            case BarcodeFormat.AZTEC:
                return R.string.barcode_name_aztec;

            case BarcodeFormat.CODABAR:
                return R.string.barcode_name_codabar;

            case BarcodeFormat.CODE_128:
                return R.string.barcode_name_code_128;

            case BarcodeFormat.CODE_39:
                return R.string.barcode_name_code_39;

            case BarcodeFormat.CODE_93:
                return R.string.barcode_name_code_93;

            case BarcodeFormat.DATA_MATRIX:
                return R.string.barcode_name_data_matrix;

            case BarcodeFormat.EAN_13:
                return R.string.barcode_name_ean_13;

            case BarcodeFormat.EAN_8:
                return R.string.barcode_name_ean_8;

            case BarcodeFormat.ITF:
                return R.string.barcode_name_itf;

            case BarcodeFormat.MAXICODE:
                return R.string.barcode_name_maxi_code;

            case BarcodeFormat.PDF_417:
                return R.string.barcode_name_pdf_417;

            case BarcodeFormat.QR_CODE:
                return R.string.barcode_name_qr_code;

            case BarcodeFormat.RSS_14:
                return R.string.barcode_name_rss_14;

            case BarcodeFormat.RSS_EXPANDED:
                return R.string.barcode_name_rss_expanded;

            case BarcodeFormat.UPC_A:
                return R.string.barcode_name_upc_a;

            case BarcodeFormat.UPC_EAN_EXTENSION:
                return R.string.barcode_name_upc_ean_extension;

            case BarcodeFormat.UPC_E:
                return R.string.barcode_name_upc_e;

            case BarcodeFormat.NONE:
                return R.string.barcode_name_none;

            default:
                throw new IllegalStateException("Encountered unknown barcode format: " + format);
        }
    }
}
