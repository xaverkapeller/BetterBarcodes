package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 01/04/16
 */
class SimpleDecodeBarcodeImageDecoder extends BaseBarcodeImageDecoder {

    SimpleDecodeBarcodeImageDecoder(int orientation, Reader reader) {
        super(orientation, reader);
    }

    @Override
    protected Result performDecode(Reader reader, BinaryBitmap bitmap) throws NotFoundException, ChecksumException, FormatException {
        return reader.decode(bitmap);
    }
}
