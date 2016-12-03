package com.github.wrdlbrnft.betterbarcodes.writer;

import android.graphics.Bitmap;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 05/02/16
 */
@KeepClass
@KeepClassMembers
public interface BarcodeWriter {
    Bitmap write(String text, int width, int height);
}
