package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import android.media.Image;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 01/04/16
 */
@KeepClass
@KeepClassMembers
public interface BarcodeImageDecoder {
    void setFormat(@BarcodeFormat int... format);
    BarcodeResult decode(byte[] data, int width, int height);
    BarcodeResult decode(Image image);
}
