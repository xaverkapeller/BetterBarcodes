package com.github.wrdlbrnft.betterbarcodes.writer;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;

/**
 * Created by kapeller on 30/03/16.
 */
class Code128WriterImpl extends AbsZXingBarcodeWriter {

    @Override
    public Bitmap write(String text, int width, int height) {

        final int barcodeHeight = height * 2 / 3;
        final int y = (height - barcodeHeight) / 2;

        final int[] pixels = getBarcodePixels(BarcodeFormat.CODE_128, text, width, barcodeHeight);

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, y, width, barcodeHeight);
        return bitmap;
    }
}
