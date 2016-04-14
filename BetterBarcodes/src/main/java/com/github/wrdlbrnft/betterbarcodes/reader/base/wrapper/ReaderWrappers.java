package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatConverter;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kapeller on 01/04/16.
 */
public class ReaderWrappers {

    public static ReaderWrapper forFormat(@BarcodeFormat int... format) {
        final MultiFormatReader reader = new MultiFormatReader();
        final Map<DecodeHintType, Object> hints = new ArrayMap<>();
        hints.put(DecodeHintType.POSSIBLE_FORMATS, toZXingBarcodeFormatList(format));
        reader.setHints(hints);
        return new SimpleDecodeReaderWrapper(reader);
    }

    @NonNull
    private static List<com.google.zxing.BarcodeFormat> toZXingBarcodeFormatList(@BarcodeFormat int[] formats) {
        final List<com.google.zxing.BarcodeFormat> zxingFormats = new ArrayList<>();

        for (int format : formats) {
            zxingFormats.add(FormatConverter.toZXing(format));
        }

        return zxingFormats;
    }
}
