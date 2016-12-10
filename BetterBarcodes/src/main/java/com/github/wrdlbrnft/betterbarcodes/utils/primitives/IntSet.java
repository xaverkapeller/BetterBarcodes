package com.github.wrdlbrnft.betterbarcodes.utils.primitives;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 05/12/2016
 */
public class IntSet {

    public static final int DEFAULT_CAPACITY = 8;

    private int[] mValues;
    private int mSize = 0;

    public IntSet(int capacity) {
        mValues = new int[capacity];
    }

    public IntSet() {
        this(DEFAULT_CAPACITY);
    }

    public boolean contains(int value) {
        final int index = ContainerHelpers.binarySearch(mValues, mSize, value);
        return index >= 0;
    }

    public boolean add(int value) {
        int index = ContainerHelpers.binarySearch(mValues, mSize, value);

        if (index >= 0) {
            return false;
        }
        index = ~index;

        mValues = insert(mValues, mSize, index, value);
        mSize++;

        return true;
    }

    public int[] toArray() {
        final int[] result = new int[mSize];
        System.arraycopy(mValues, 0, result, 0, mSize);
        return result;
    }

    private static int[] insert(int[] array, int currentSize, int index, int element) {
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }

        final int[] newArray = new int[growSize(currentSize)];
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    private static int growSize(int currentSize) {
        return currentSize <= 4 ? DEFAULT_CAPACITY : currentSize * 2;
    }
}
