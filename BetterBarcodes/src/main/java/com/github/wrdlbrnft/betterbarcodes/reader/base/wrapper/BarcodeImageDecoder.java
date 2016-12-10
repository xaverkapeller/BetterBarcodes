package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

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
    String decode(byte[] data, int width, int height) throws NotFoundException, ChecksumException, FormatException;
    void reset();
}
