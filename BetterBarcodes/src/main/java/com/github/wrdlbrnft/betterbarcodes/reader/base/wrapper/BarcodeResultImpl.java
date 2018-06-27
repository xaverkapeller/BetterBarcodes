package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

class BarcodeResultImpl implements BarcodeResult {

    private final String mText;

    BarcodeResultImpl(String text) {
        mText = text;
    }

    @Override
    public String getText() {
        return mText;
    }
}
