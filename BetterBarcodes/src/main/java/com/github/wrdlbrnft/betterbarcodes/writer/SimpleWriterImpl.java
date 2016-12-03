package com.github.wrdlbrnft.betterbarcodes.writer;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 30/03/16
 */
class SimpleWriterImpl extends AbsZXingBarcodeWriter {

    private final BarcodeFormat mFormat;

    SimpleWriterImpl(BarcodeFormat format) {
        mFormat = format;
    }

    @Override
    public Bitmap write(String text, int width, int height) {
        final int[] pixels = getBarcodePixels(mFormat, text, width, height);

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
