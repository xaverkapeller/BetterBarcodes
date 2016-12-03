package com.github.wrdlbrnft.betterbarcodes.views.writer;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 01/05/16
 */
class BarcodeInfo {
    public final int format;
    public final String text;
    public final int width;
    public final int height;

    BarcodeInfo(int format, String text, int width, int height) {
        this.format = format;
        this.text = text;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BarcodeInfo that = (BarcodeInfo) o;

        if (format != that.format) return false;
        if (width != that.width) return false;
        if (height != that.height) return false;
        return text != null ? text.equals(that.text) : that.text == null;

    }

    @Override
    public int hashCode() {
        int result = format;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}
