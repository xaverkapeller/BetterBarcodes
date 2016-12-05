package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatUtils;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kapeller on 01/04/16.
 */
public class ReaderWrappers {

    public static ReaderWrapper forFormat(Context context, @BarcodeFormat int... format) {
        final MultiFormatReader reader = createZXingBarcodeReader(format);
        final int orientation = getOrientation(context);
        return new SimpleDecodeReaderWrapper(orientation, reader);
    }

    private static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    @NonNull
    private static MultiFormatReader createZXingBarcodeReader(@BarcodeFormat int[] format) {
        final MultiFormatReader reader = new MultiFormatReader();
        final Map<DecodeHintType, Object> hints = new ArrayMap<>();
        hints.put(DecodeHintType.POSSIBLE_FORMATS, toZXingBarcodeFormatList(FormatUtils.split(format)));
        reader.setHints(hints);
        return reader;
    }

    @NonNull
    private static List<com.google.zxing.BarcodeFormat> toZXingBarcodeFormatList(@BarcodeFormat int[] formats) {
        final List<com.google.zxing.BarcodeFormat> zxingFormats = new ArrayList<>();

        for (int format : formats) {
            zxingFormats.add(FormatUtils.toZXing(format));
        }

        return zxingFormats;
    }
}
