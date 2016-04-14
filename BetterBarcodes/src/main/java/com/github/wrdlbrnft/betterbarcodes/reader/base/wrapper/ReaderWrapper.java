package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

/**
 * Created by kapeller on 01/04/16.
 */
public interface ReaderWrapper {
    String decode(byte[] data, int width, int height) throws NotFoundException, ChecksumException, FormatException;
    void reset();
}
