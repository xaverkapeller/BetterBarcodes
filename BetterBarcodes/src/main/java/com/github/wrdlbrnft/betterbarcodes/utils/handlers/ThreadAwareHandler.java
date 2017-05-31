package com.github.wrdlbrnft.betterbarcodes.utils.handlers;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.github.wrdlbrnft.betterbarcodes.utils.handlers.exceptions.ThreadNotRunningException;

import static com.github.wrdlbrnft.betterbarcodes.utils.handlers.HandlerThreadUtils.createHandler;
import static com.github.wrdlbrnft.betterbarcodes.utils.handlers.HandlerThreadUtils.ensureThreadIsRunning;
import static com.github.wrdlbrnft.betterbarcodes.utils.handlers.HandlerThreadUtils.isRunning;
import static com.github.wrdlbrnft.betterbarcodes.utils.handlers.HandlerThreadUtils.quitSafely;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 30/05/2017
 */

public class ThreadAwareHandler extends Handler {

    private final Object LOCK = new Object();

    private final String mThreadName;

    private volatile HandlerThread mThread;
    private volatile Handler mHandler;

    public ThreadAwareHandler(String threadName) {
        super();
        mThreadName = threadName;
    }

    public ThreadAwareHandler(String threadName, Callback callback) {
        super(callback);
        mThreadName = threadName;
    }

    @Override
    public void handleMessage(Message msg) {
        synchronized (LOCK) {
            if (isRunning(mThread) && mHandler != null) {
                mHandler.dispatchMessage(msg);
            } else {
                throw new ThreadNotRunningException("The Thread backed by this Handler is not running. Ensure that you called startThread() before using it.");
            }
        }
    }

    @Override
    public void dispatchMessage(Message msg) {
        synchronized (LOCK) {
            if (isRunning(mThread) && mHandler != null) {
                mHandler.dispatchMessage(msg);
            } else {
                throw new ThreadNotRunningException("The Thread backed by this Handler is not running. Ensure that you called startThread() before using it.");
            }
        }
    }

    public void clearCallbacks(Object token) {
        synchronized (LOCK) {
            if (isRunning(mThread) && mHandler != null) {
                mHandler.removeCallbacksAndMessages(token);
            }
        }
    }

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        synchronized (LOCK) {
            if (isRunning(mThread) && mHandler != null) {
                return mHandler.sendMessageAtTime(msg, uptimeMillis);
            }
        }
        return false;
    }

    public void startThread() {
        synchronized (LOCK) {
            mThread = ensureThreadIsRunning(mThread, mThreadName);
            mHandler = createHandler(mThread);
        }
    }

    public void stopThread() {
        synchronized (LOCK) {
            quitSafely(mThread);
            mHandler = null;
        }
    }
}
