package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.zxing;

import com.google.zxing.LuminanceSource;

class PlanarYUVLuminanceSource extends LuminanceSource {

    public static PlanarYUVLuminanceSource fromPortrait(byte[] data, int width, int height) {
        final byte[] result = new byte[data.length];
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                result[row + column * height] = data[column + row * width];
            }
        }
        //noinspection SuspiciousNameCombination
        return new PlanarYUVLuminanceSource(result, height, width);
    }

    public static PlanarYUVLuminanceSource fromLandscape(byte[] data, int width, int height) {
        //noinspection SuspiciousNameCombination
        return new PlanarYUVLuminanceSource(data, width, height);
    }

    private final byte[] mYuvData;

    private PlanarYUVLuminanceSource(byte[] yuvData, int width, int height) {
        super(width, height);

        mYuvData = yuvData;
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        final int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        final int offset = y * width;
        System.arraycopy(mYuvData, offset, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        return mYuvData;
    }
}
