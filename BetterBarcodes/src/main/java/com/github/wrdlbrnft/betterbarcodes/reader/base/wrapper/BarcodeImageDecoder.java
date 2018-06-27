package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import android.media.Image;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

import java.util.List;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 01/04/16
 */
@KeepClass
@KeepClassMembers
public interface BarcodeImageDecoder {

    int ORIENTATION_0 = 0x01;
    int ORIENTATION_90 = 0x02;
    int ORIENTATION_180 = 0x04;
    int ORIENTATION_270 = 0x08;

    @IntDef({ORIENTATION_0, ORIENTATION_90, ORIENTATION_180, ORIENTATION_270})
    @interface Orientation {}

    void setFormat(@BarcodeFormat int... format);
    @NonNull
    List<BarcodeResult> decode(@Orientation int orientation, byte[] data, int width, int height);
    @NonNull
    List<BarcodeResult> decode(@Orientation int orientation, Image image);
}
