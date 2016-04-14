package com.github.wrdlbrnft.betterbarcodes.writer;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatConverter;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created by kapeller on 30/03/16.
 */
@KeepClass
@KeepClassMembers
public class BarcodeWriters {

    public static BarcodeWriter forFormat(@BarcodeFormat int format) {
        switch (format) {

            case BarcodeFormat.CODE_128:
                return new Code128WriterImpl();

            default:
                final com.google.zxing.BarcodeFormat zxingFormat = FormatConverter.toZXing(format);
                return new BasicWriterImpl(zxingFormat);
        }
    }
}
