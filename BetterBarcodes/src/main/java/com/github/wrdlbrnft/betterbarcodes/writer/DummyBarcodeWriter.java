package com.github.wrdlbrnft.betterbarcodes.writer;

import android.graphics.Bitmap;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 07/12/2016
 */
class DummyBarcodeWriter implements BarcodeWriter {

    @Override
    public Bitmap write(String text, int width, int height) {
        return null;
    }
}
