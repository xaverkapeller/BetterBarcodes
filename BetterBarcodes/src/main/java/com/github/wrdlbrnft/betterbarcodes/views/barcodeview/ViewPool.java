package com.github.wrdlbrnft.betterbarcodes.views.barcodeview;

import android.view.View;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 24/04/16
 */
public interface ViewPool<T extends View> {
    T claimView();
    void returnView(T view);
}
