package com.github.wrdlbrnft.betterbarcodes.utils.handlers.exceptions;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 30/05/2017
 */

public class ThreadNotRunningException extends RuntimeException {

    public ThreadNotRunningException(String message) {
        super(message);
    }

    public ThreadNotRunningException(String message, Throwable cause) {
        super(message, cause);
    }
}
