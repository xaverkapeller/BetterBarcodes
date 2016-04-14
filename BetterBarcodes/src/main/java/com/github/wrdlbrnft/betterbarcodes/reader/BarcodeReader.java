package com.github.wrdlbrnft.betterbarcodes.reader;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.reader.permissions.PermissionHandler;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created by kapeller on 25/01/16.
 */
@KeepClass
@KeepClassMembers
public interface BarcodeReader {

    @KeepClassMembers
    interface Callback {
        void onResult(String text);
    }

    void startPreview();
    void startScanning();
    void stopScanning();
    void stopPreview();
    void setFormat(@BarcodeFormat int... format);
    void setCallback(Callback callback);
    void setCameraPermissionHandler(PermissionHandler handler);
}
