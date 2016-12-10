package com.github.wrdlbrnft.betterbarcodes.utils;

import android.util.SparseArray;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 11/04/16
 */
public class SparseArrayBuilder<T> {

    private final SparseArray<T> mArray = new SparseArray<>();

    public SparseArrayBuilder<T> put(int key, T item) {
        mArray.put(key, item);
        return this;
    }

    public SparseArray<T> build() {
        return mArray;
    }
}
