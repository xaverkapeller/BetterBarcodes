package com.github.wrdlbrnft.betterbarcodes.reader.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Surface;
import android.view.WindowManager;

import com.github.wrdlbrnft.betterbarcodes.reader.BarcodeReader;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeImageDecoder;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeResult;
import com.github.wrdlbrnft.betterbarcodes.reader.permissions.PermissionHandler;
import com.github.wrdlbrnft.betterbarcodes.reader.permissions.PermissionRequest;
import com.github.wrdlbrnft.betterbarcodes.utils.handlers.ThreadAwareHandler;
import com.github.wrdlbrnft.simpletasks.runners.SimpleTaskRunner;
import com.github.wrdlbrnft.simpletasks.runners.TaskRunner;
import com.github.wrdlbrnft.simpletasks.tasks.Task;

import java.util.List;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 25/01/16
 */
public abstract class BaseBarcodeReader implements BarcodeReader {

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final String TAG = "BaseBarcodeReader";

    private static final PermissionHandler DUMMY_PERMISSION_HANDLER = new PermissionHandler.Adapter() {
        @Override
        public void onNewPermissionRequest(PermissionRequest request) {
            throw new IllegalStateException("You need to set the PermissionHandler to handle runtime " +
                    "permission on devices running Android 6.0 (Marshmallow) or newer. " +
                    "You can also always just request the permission yourself before using the barcode reader."
            );
        }
    };

    private static final Callback DUMMY_READER_CALLBACK = token -> {
    };

    public static final int STATE_PERMISSION_MISSING = 0x00;
    public static final int STATE_PERMISSION_REQUIRED = 0x01;
    public static final int STATE_STOPPED = 0x02;
    public static final int STATE_PREVIEWING = 0x04;
    public static final int STATE_SCANNING = 0x08;

    @IntDef({STATE_PERMISSION_MISSING, STATE_PERMISSION_REQUIRED, STATE_STOPPED, STATE_PREVIEWING, STATE_SCANNING})
    public @interface State {
    }

    private final Context mContext;
    private PermissionHandler mPermissionHandler = DUMMY_PERMISSION_HANDLER;
    private Callback mCallback = DUMMY_READER_CALLBACK;
    private final WindowManager mWindowManager;
    private final ThreadAwareHandler mCameraHandler = new ThreadAwareHandler("BarcodeReaderCameraThread");
    private final ThreadAwareHandler mProcessingHandler = new ThreadAwareHandler("BarcodeReaderProcessingThread");
    private BarcodeImageDecoder mReader;

    public interface CameraInfo {
        int getSensorOrientation();
        boolean isFrontFacing();
        boolean isBackFacing();
    }

    @State
    private volatile int mState = STATE_STOPPED;

    protected BaseBarcodeReader(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mContext = context;
    }

    @Override
    public final void startPreview() {
        if (mState != STATE_STOPPED) {
            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mState = STATE_PERMISSION_REQUIRED;
            requestPermission();
            return;
        }

        startBackgroundThread();
        onStartPreview();
        mState = STATE_PREVIEWING;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        mPermissionHandler.onNewPermissionRequest(new PermissionRequestImpl(Manifest.permission.CAMERA, mPermissionHandler));
    }

    @Override
    public final void stopPreview() {
        if (mState > STATE_PREVIEWING) {
            stopScanning();
        }

        if (mState > STATE_STOPPED) {
            onStopPreview();
            mState = STATE_STOPPED;
            stopBackgroundThread();
        }
    }

    @Override
    public final void startScanning() {

        if (mState < STATE_STOPPED) {
            return;
        }

        if (mState < STATE_PREVIEWING) {
            startPreview();
        }

        if (mState < STATE_SCANNING) {
            onStartScanning();
            mState = STATE_SCANNING;
        }
    }

    @Override
    public final void stopScanning() {
        if (mState < STATE_SCANNING) {
            return;
        }

        onStopScanning();
        mState = STATE_PREVIEWING;
    }

    @Override
    public void setBarcodeImageDecoder(BarcodeImageDecoder decoder) {
        mReader = decoder;
    }

    protected abstract void onStartPreview();
    protected abstract void onStartScanning();
    protected abstract void onStopScanning();
    protected abstract void onStopPreview();

    protected abstract CameraInfo getCameraInfo();

    private void startBackgroundThread() {
        mCameraHandler.startThread();
        mProcessingHandler.startThread();
    }

    private void stopBackgroundThread() {
        mCameraHandler.stopThread();
        mProcessingHandler.stopThread();
    }

    @State
    public int getState() {
        return mState;
    }

    private synchronized void notifyResult(List<BarcodeResult> results) {
        if(mState >= STATE_SCANNING) {
            stopScanning();
            postOnMainThread(() -> mCallback.onResult(results));
            mCameraHandler.clearCallbacks(null);
        }
    }

    @BarcodeImageDecoder.Orientation
    private int getDecoderOrientation() {
        final int displayOrientation = getDisplayRotation();
        final CameraInfo cameraInfo = getCameraInfo();
        final int sensorOrientation = cameraInfo.getSensorOrientation();
        final int relativeAngle = getRelativeImageOrientation(
                displayOrientation,
                sensorOrientation,
                cameraInfo.isFrontFacing(),
                false
        );
        return convertAngleToDecoderOrientation(relativeAngle);
    }

    protected Task<List<BarcodeResult>> submitImageData(byte[] data, int width, int height) {
        if (mReader == null) {
            return Task.withError(null);
        }

        final int orientation = getDecoderOrientation();
        return mReader.decode(orientation, data, width, height)
                .onResult(results -> {
                    if (!results.isEmpty() && mState >= STATE_SCANNING) {
                        notifyResult(results);
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected Task<List<BarcodeResult>> submitImageData(Image image) {
        if (mReader == null) {
            image.close();
            return Task.withError(null);
        }

        final int orientation = getDecoderOrientation();
        return mReader.decode(orientation, image)
                .onResult(results -> {
                    if (!results.isEmpty()) {
                        notifyResult(results);
                    }
                });
    }

    protected Handler getCameraHandler() {
        return mCameraHandler;
    }

    protected Handler getProcessingHandler() {
        return mProcessingHandler;
    }

    protected void postOnMainThread(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }

    protected void postOnMainThread(long delay, Runnable runnable) {
        MAIN_HANDLER.postDelayed(runnable, delay);
    }

    protected void postOnCameraThread(Runnable runnable) {
        mCameraHandler.post(runnable);
    }

    protected void postOnCameraThread(long delay, Runnable runnable) {
        mCameraHandler.postDelayed(runnable, delay);
    }

    protected void postOnProcessingThread(Runnable runnable) {
        if (getState() == STATE_SCANNING) {
            mProcessingHandler.post(runnable);
        }
    }

    protected void postOnProcessingThread(long delay, Runnable runnable) {
        mProcessingHandler.postDelayed(runnable, delay);
    }

    @Override
    public void setCameraPermissionHandler(PermissionHandler permissionHandler) {
        mPermissionHandler = permissionHandler;
    }

    @Override
    public void setCallback(Callback callback) {
        mCallback = callback != null ? callback : DUMMY_READER_CALLBACK;
    }

    public BarcodeImageDecoder getReader() {
        return mReader;
    }

    @BarcodeImageDecoder.Orientation
    private int convertAngleToDecoderOrientation(int angle) {
        final int bracket = ((angle + 45) / 90) % 4;
        switch (bracket) {
            case 0:
                return BarcodeImageDecoder.ORIENTATION_0;
            case 1:
                return BarcodeImageDecoder.ORIENTATION_90;
            case 2:
                return BarcodeImageDecoder.ORIENTATION_180;
            case 3:
                return BarcodeImageDecoder.ORIENTATION_270;
            default:
                return BarcodeImageDecoder.ORIENTATION_90;
        }
    }

    private int getRelativeImageOrientation(int displayRotation, int sensorOrientation, boolean isFrontFacing, boolean compensateForMirroring) {
        int result;
        if (isFrontFacing) {
            result = (sensorOrientation + displayRotation) % 360;
            if (compensateForMirroring) {
                result = (360 - result) % 360;
            }
        } else {
            result = (sensorOrientation - displayRotation + 360) % 360;
        }
        return result;
    }

    private int getDisplayRotation() {
        final int rotation = mWindowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    private class PermissionRequestImpl implements PermissionRequest {

        private final String mPermission;
        private final int mRequestCode;
        private final PermissionHandler mHandler;

        public PermissionRequestImpl(String permission, PermissionHandler handler) {
            mPermission = permission;
            mHandler = handler;
            mRequestCode = permission.hashCode() & 0xFF;
        }

        @Override
        public void start(Activity activity) {
            if (isPermissionGranted(activity)) {
                notifyGranted();
                return;
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, mPermission) && mHandler.onShowRationale()) {
                return;
            }

            continueAfterRationale(activity);
        }

        @Override
        public void continueAfterRationale(Activity activity) {
            ActivityCompat.requestPermissions(activity, new String[]{mPermission}, mRequestCode);
        }

        @Override
        public void start(Fragment fragment) {
            if (isPermissionGranted(fragment.getContext())) {
                notifyGranted();
                return;
            }

            if (fragment.shouldShowRequestPermissionRationale(mPermission) && mHandler.onShowRationale()) {
                return;
            }

            continueAfterRationale(fragment);
        }

        @Override
        public void continueAfterRationale(Fragment fragment) {
            fragment.requestPermissions(new String[]{mPermission}, mRequestCode);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == mRequestCode) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mState = STATE_STOPPED;
                    startPreview();
                    notifyGranted();
                } else {
                    mState = STATE_PERMISSION_MISSING;
                    notifyDenied();
                }
            }
        }

        private boolean isPermissionGranted(Context context) {
            return ContextCompat.checkSelfPermission(context, mPermission) == PackageManager.PERMISSION_GRANTED;
        }

        private void notifyGranted() {
            mHandler.onPermissionGranted();
        }

        private void notifyDenied() {
            mHandler.onPermissionDenied();
        }
    }
}
