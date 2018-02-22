package com.github.wrdlbrnft.betterbarcodes.utils.handlers;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 30/05/2017
 */

public class HandlerThreadUtils {

    private static final String TAG = "HandlerThreadUtils";

    public static void quitSafely(HandlerThread thread) {
        if (thread == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            thread.quitSafely();
        } else {
            thread.quit();
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.i(TAG, "Failed to properly terminate background thread.", e);
        }
    }

    public static HandlerThread ensureThreadIsRunning(HandlerThread currentThread, String name) {
        if (currentThread != null) {
            if (currentThread.isAlive()) {
                return currentThread;
            } else {
                HandlerThreadUtils.quitSafely(currentThread);
            }
        }

        return startNewThread(name);
    }

    public static HandlerThread startNewThread(String name) {
        final HandlerThread newThread = new HandlerThread(name);
        newThread.start();
        return newThread;
    }

    public static Handler createHandler(HandlerThread thread) {
        return new Handler(thread.getLooper());
    }

    public static boolean isRunning(HandlerThread thread) {
        return thread != null && thread.isAlive();

    }
}
