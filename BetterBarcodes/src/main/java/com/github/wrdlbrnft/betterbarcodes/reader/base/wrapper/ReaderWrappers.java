package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import android.content.Context;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatUtils;
import com.google.zxing.Reader;

/**
 * Created by kapeller on 01/04/16.
 */
public class ReaderWrappers {

    public static ReaderWrapper forFormat(Context context, @BarcodeFormat int... format) {
        final Reader reader = new MultiFormatZXingReader(FormatUtils.split(format));
        final int orientation = getOrientation(context);
        return new SimpleDecodeReaderWrapper(orientation, reader);
    }

    private static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }
}
