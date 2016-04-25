package com.github.wrdlbrnft.betterbarcodes.views.barcodeview;

import android.view.View;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 24/04/16
 */
abstract class AbsViewPool<T extends View> implements ViewPool<T> {

    private final Queue<T> mViewQueue = new ArrayDeque<>();

    @Override
    public T claimView() {
        final T view = mViewQueue.poll();
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            return view;
        }

        return createView();
    }

    @Override
    public void returnView(T view) {
        view.setVisibility(View.GONE);
        mViewQueue.add(view);
    }

    protected abstract T createView();
}
