package com.github.wrdlbrnft.betterbarcodes.writer;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatUtils;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 30/03/16
 */
@KeepClass
@KeepClassMembers
public class BarcodeWriters {

    public static BarcodeWriter forFormat(@BarcodeFormat int format) {
        if (format == BarcodeFormat.NONE) {
            return new DummyBarcodeWriter();
        }

        final com.google.zxing.BarcodeFormat zxingFormat = FormatUtils.toZXing(format);
        return new SimpleWriterImpl(zxingFormat);
    }
}
