package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

@KeepClass
@KeepClassMembers
public interface BarcodeResult {
    boolean isSuccess();
    String getText();

    static BarcodeResult ofSuccess(String text) {
        return new BarcodeResultImpl(true, text);
    }

    static BarcodeResult ofError() {
        return new BarcodeResultImpl(true, null);
    }
}
