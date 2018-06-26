package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

class BarcodeResultImpl implements BarcodeResult {

    private final boolean mSuccess;
    private final String mText;

    BarcodeResultImpl(boolean success, String text) {
        mSuccess = success;
        mText = text;
    }

    @Override
    public boolean isSuccess() {
        return mSuccess;
    }

    @Override
    public String getText() {
        return mText;
    }
}
