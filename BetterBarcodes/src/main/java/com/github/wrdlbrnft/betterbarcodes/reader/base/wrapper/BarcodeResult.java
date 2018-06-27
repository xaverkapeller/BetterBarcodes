package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

@KeepClass
@KeepClassMembers
public interface BarcodeResult {
    String getText();

    static BarcodeResult of(String text) {
        return new BarcodeResultImpl(text);
    }
}
