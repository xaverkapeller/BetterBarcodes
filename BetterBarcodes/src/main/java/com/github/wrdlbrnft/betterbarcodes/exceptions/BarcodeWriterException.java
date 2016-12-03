package com.github.wrdlbrnft.betterbarcodes.exceptions;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 05/02/16
 */
@KeepClass
@KeepClassMembers
public class BarcodeWriterException extends RuntimeException {

    public BarcodeWriterException(String detailMessage) {
        super(detailMessage);
    }

    public BarcodeWriterException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
