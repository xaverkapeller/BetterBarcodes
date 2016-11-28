package com.github.wrdlbrnft.betterbarcodes.views.writer;

import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.LruCache;

import com.github.wrdlbrnft.betterbarcodes.writer.BarcodeWriter;
import com.github.wrdlbrnft.betterbarcodes.writer.BarcodeWriters;

/**
 * Created by kapeller on 28/11/16.
 */
class BarcodeImageCache extends LruCache<BarcodeInfo, Bitmap> {

    public BarcodeImageCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(BarcodeInfo info, android.graphics.Bitmap value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return value.getAllocationByteCount();
        }

        return value.getByteCount();
    }

    @Override
    protected android.graphics.Bitmap create(BarcodeInfo info) {
        final BarcodeWriter writer = BarcodeWriters.forFormat(info.format);
        if (TextUtils.isEmpty(info.text)) {
            return null;
        }
        return writer.write(info.text, info.width, info.height);
    }
}
