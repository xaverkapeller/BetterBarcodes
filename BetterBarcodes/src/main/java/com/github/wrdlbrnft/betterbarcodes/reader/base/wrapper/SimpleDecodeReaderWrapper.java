package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;

/**
 * Created by kapeller on 01/04/16.
 */
class SimpleDecodeReaderWrapper extends BaseReaderWrapper {

    public SimpleDecodeReaderWrapper(Reader reader) {
        super(reader);
    }

    @Override
    protected Result performDecode(Reader reader, BinaryBitmap bitmap) throws NotFoundException, ChecksumException, FormatException {
        return reader.decode(bitmap);
    }
}
