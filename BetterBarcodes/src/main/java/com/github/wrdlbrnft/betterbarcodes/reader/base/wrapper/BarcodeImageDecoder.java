package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import android.media.Image;
import android.support.annotation.IntDef;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.simpletasks.tasks.Task;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ORIENTATION_0, ORIENTATION_90, ORIENTATION_180, ORIENTATION_270})
    @interface Orientation {
    }

    void setFormat(@BarcodeFormat int... format);
    Task<List<BarcodeResult>> decode(@Orientation int orientation, byte[] data, int width, int height);
    Task<List<BarcodeResult>> decode(@Orientation int orientation, Image image);
}
