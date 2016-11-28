package com.github.wrdlbrnft.betterbarcodes.views.writer;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by kapeller on 28/11/16.
 */
class BinderImpl implements BarcodeView.Binder<ImageView, BarcodeInfo> {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final LruCache<BarcodeInfo, Bitmap> mCache;

    BinderImpl(LruCache<BarcodeInfo, Bitmap> cache) {
        mCache = cache;
    }

    @Override
    public Future<Bitmap> bind(ImageView view, BarcodeInfo data) {
        final FutureTask<Bitmap> task = new FutureTask<>(new BarcodeCallable(mCache, view, data));
        EXECUTOR.execute(task);
        return task;
    }
}
