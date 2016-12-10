package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import android.content.Context;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatUtils;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;
import com.google.zxing.Reader;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 01/04/16
 */
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class BarcodeImageDecoders {

    public static BarcodeImageDecoder forFormat(Context context, @BarcodeFormat int... format) {
        final Reader reader = new MultiFormatZXingReader(FormatUtils.split(format));
        final int orientation = getOrientation(context);
        return new SimpleDecodeBarcodeImageDecoder(orientation, reader);
    }

    private static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }
}
