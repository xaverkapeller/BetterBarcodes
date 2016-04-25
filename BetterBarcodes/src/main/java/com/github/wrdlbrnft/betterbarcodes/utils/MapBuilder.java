package com.github.wrdlbrnft.betterbarcodes.utils;

import android.support.v4.util.ArrayMap;

import java.util.Collections;
import java.util.Map;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 23/04/16
 */
public class MapBuilder<K, V> {

    private final Map<K, V> mMap = new ArrayMap<>();

    public MapBuilder<K, V> put(K key, V value) {
        mMap.put(key, value);
        return this;
    }

    public Map<K, V> build() {
        return Collections.unmodifiableMap(mMap);
    }
}
