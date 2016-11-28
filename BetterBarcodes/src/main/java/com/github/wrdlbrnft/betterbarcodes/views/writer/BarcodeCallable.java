package com.github.wrdlbrnft.betterbarcodes.views.writer;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

/**
 * Created by kapeller on 28/11/16.
 */
class BarcodeCallable implements Callable<Bitmap> {

    private final LruCache<BarcodeInfo, Bitmap> mCache;
    private final BarcodeInfo mBarcodeInfo;
    private final WeakReference<ImageView> mViewReference;

    BarcodeCallable(LruCache<BarcodeInfo, Bitmap> cache, ImageView view, BarcodeInfo barcodeInfo) {
        mCache = cache;
        mViewReference = new WeakReference<>(view);
        mBarcodeInfo = barcodeInfo;
    }

    @Override
    public Bitmap call() throws Exception {
        final ImageView view = mViewReference.get();
        final Bitmap bitmap = mCache.get(mBarcodeInfo);
        if (view != null) {
            view.setImageBitmap(bitmap);
        }
        return bitmap;
    }
}
