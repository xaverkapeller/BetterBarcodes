package com.github.wrdlbrnft.betterbarcodes.reader.icecreamsandwich;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Display;
import android.view.TextureView;
import android.view.WindowManager;

import com.github.wrdlbrnft.betterbarcodes.reader.base.BaseBarcodeReader;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeImageDecoder;
import com.github.wrdlbrnft.betterbarcodes.views.AspectRatioTextureView;
import com.google.zxing.ReaderException;

import java.io.IOException;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 25/01/16
 */
public class IceCreamSandwichBarcodeReader extends BaseBarcodeReader {

    private Camera mCamera;
    private boolean mCameraActive = false;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            startCamera(texture);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
//            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {

        }
    };

    private final Camera.PreviewCallback mPreviewCallback = (data, camera) -> {
        final Camera.Parameters parameters = camera.getParameters();
        final Camera.Size size = parameters.getPreviewSize();
        postOnProcessingThread(new Runnable() {
            @Override
            public void run() {
                final BarcodeImageDecoder reader = getCurrentReader();
                try {
                    final String text = reader.decode(data, size.width, size.height);
                    notifyResult(text);
                } catch (ReaderException | NullPointerException | ArrayIndexOutOfBoundsException ignored) {
                } finally {
                    reader.reset();
                }
                postOnCameraThread(() -> {
                    if (getState() == STATE_SCANNING) {
                        mCamera.setOneShotPreviewCallback(mPreviewCallback);
                    }
                });
            }
        });
    };

    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            postOnCameraThread(1000L, mAutoFocusRunnable);
        }
    };

    private final Runnable mAutoFocusRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCamera != null && mCameraActive) {
                mCamera.autoFocus(mAutoFocusCallback);
            }
        }
    };

    private final AspectRatioTextureView mTextureView;
    private final WindowManager mWindowManager;

    public IceCreamSandwichBarcodeReader(Context context, AspectRatioTextureView textureView) {
        super(context);
        mTextureView = textureView;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    protected void onStartPreview() {
        if (mTextureView.isAvailable()) {
            startCamera(mTextureView.getSurfaceTexture());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void startCamera(SurfaceTexture surfaceTexture) {
        try {
            mTextureView.setAspectRatio(mTextureView.getWidth(), mTextureView.getWidth() * 16 / 10);
            mCamera = Camera.open();
            if (mCamera != null) {
                mCameraActive = true;
                mCamera.setDisplayOrientation(getDisplayOrientation());
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.startPreview();
                mCamera.setOneShotPreviewCallback(mPreviewCallback);
                mCamera.autoFocus(mAutoFocusCallback);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to set preview texture", e);
        }
    }

    @Override
    protected void onStartScanning() {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(mPreviewCallback);
        }
    }

    @Override
    protected void onStopScanning() {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(null);
        }
    }

    @Override
    protected void onStopPreview() {
        mCameraActive = false;
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private int getDisplayOrientation() {
        final Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        final Display display = mWindowManager.getDefaultDisplay();
        final int rotation = display.getRotation();
        final short degrees = getDegreesFromDisplayRotation(rotation);
        return calculateDisplayOrientation(info, degrees);
    }

    private short getDegreesFromDisplayRotation(int rotation) {
        switch (rotation) {
            case 0:
                return 0;

            case 1:
                return 90;

            case 2:
                return 180;

            case 3:
                return 270;

            default:
                return 0;
        }
    }

    private int calculateDisplayOrientation(Camera.CameraInfo info, short degrees) {
        if (info.facing == 1) {
            final int result = (info.orientation + degrees) % 360;
            return (360 - result) % 360;
        }

        return (info.orientation - degrees + 360) % 360;
    }
}
