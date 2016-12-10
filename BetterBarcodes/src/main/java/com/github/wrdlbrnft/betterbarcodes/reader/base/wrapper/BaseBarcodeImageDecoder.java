package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import android.content.res.Configuration;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 01/04/16
 */
abstract class BaseBarcodeImageDecoder implements BarcodeImageDecoder {

    private final int mOrientation;
    private final Reader mReader;

    protected BaseBarcodeImageDecoder(int orientation, Reader reader) {
        mOrientation = orientation;
        mReader = reader;
    }

    @Override
    public String decode(byte[] data, int width, int height) throws NotFoundException, ChecksumException, FormatException {
        final PlanarYUVLuminanceSource luminanceSource = mOrientation == Configuration.ORIENTATION_PORTRAIT
                ? PlanarYUVLuminanceSource.fromPortrait(data, width, height)
                : PlanarYUVLuminanceSource.fromLandscape(data, width, height);
        final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
        final Result result = performDecode(mReader, bitmap);
        return result.getText();
    }

    protected abstract Result performDecode(Reader reader, BinaryBitmap bitmap) throws NotFoundException, ChecksumException, FormatException;

    @Override
    public void reset() {
        mReader.reset();
    }
}
