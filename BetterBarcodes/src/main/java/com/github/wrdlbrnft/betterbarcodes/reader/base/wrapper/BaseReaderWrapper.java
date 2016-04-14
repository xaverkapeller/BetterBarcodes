package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * Created by kapeller on 01/04/16.
 */
abstract class BaseReaderWrapper implements ReaderWrapper {

    private final Reader mReader;

    protected BaseReaderWrapper(Reader reader) {
        mReader = reader;
    }

    @Override
    public String decode(byte[] data, int width, int height) throws NotFoundException, ChecksumException, FormatException {
        final PlanarYUVLuminanceSource luminanceSource = PlanarYUVLuminanceSource.fromRotate(data, width, height);
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
