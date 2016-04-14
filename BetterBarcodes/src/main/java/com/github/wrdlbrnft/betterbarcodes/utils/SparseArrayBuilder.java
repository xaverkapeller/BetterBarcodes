package com.github.wrdlbrnft.betterbarcodes.utils;

import android.util.SparseArray;

/**
 * Created by kapeller on 11/04/16.
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
