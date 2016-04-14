package com.github.wrdlbrnft.betterbarcodes.reader.lollipop;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.github.wrdlbrnft.betterbarcodes.reader.base.BaseBarcodeReader;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.ReaderWrapper;
import com.github.wrdlbrnft.betterbarcodes.views.AspectRatioTextureView;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.hardware.camera2.CameraCharacteristics.LENS_FACING;
import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
import static android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT;

/**
 * Created by kapeller on 25/01/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopBarcodeReader extends BaseBarcodeReader {

    private static final int IMAGE_FORMAT = ImageFormat.YUV_420_888;
    private static final String LOG_TAG = LollipopBarcodeReader.class.getSimpleName();

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        }
    };

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {

        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            final Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }

            if (getState() != STATE_SCANNING || !mReadyForFrame.getAndSet(false)) {
                image.close();
                return;
            }

            postOnBackgroundThread(new ReaderRunnable(image));
        }
    };

    private class ReaderRunnable implements Runnable {

        private final Image mImage;

        private ReaderRunnable(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            final ReaderWrapper reader = getCurrentReader();
            try {
                final ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                final byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                final String text = reader.decode(data, mImage.getWidth(), mImage.getHeight());
                postOnMainThread(new SuccessRunnable(text));
            } catch (NotFoundException e) {
                Log.d(LOG_TAG, "No barcode found.", e);
                postOnMainThread(new FailureRunnable());
            } catch (ChecksumException e) {
                Log.d(LOG_TAG, "Barcode checksum was incorrect.", e);
                postOnMainThread(new FailureRunnable());
            } catch (FormatException e) {
                Log.d(LOG_TAG, "Barcode Format was invalid.", e);
                postOnMainThread(new FailureRunnable());
            } finally {
                reader.reset();
                mImage.close();
            }
        }
    }

    private class SuccessRunnable implements Runnable {

        private final String mText;

        private SuccessRunnable(String text) {
            mText = text;
        }

        @Override
        public void run() {
            notifyResult(mText);
            mReadyForFrame.set(true);
        }
    }

    private class FailureRunnable implements Runnable {

        @Override
        public void run() {
            mReadyForFrame.set(true);
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private final AtomicBoolean mReadyForFrame = new AtomicBoolean(true);

    private final AspectRatioTextureView mTextureView;
    private final WindowManager mWindowManager;
    private final CameraManager mCameraManager;
    private final Resources mResources;

    private Size mPreviewSize;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private ImageReader mImageReader;
    private String mCameraId;

    public LollipopBarcodeReader(Context context, AspectRatioTextureView textureView) {
        super(context);
        mTextureView = textureView;
        mResources = context.getResources();
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    protected void onStartPreview() {
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onStartScanning() {
        mReadyForFrame.set(true);
    }

    @Override
    protected void onStopScanning() {
        mReadyForFrame.set(false);
    }

    @Override
    protected void onStopPreview() {
        closeCamera();
    }

    private void setUpCameraOutputs(int width, int height) {
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                final CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);

                //noinspection ConstantConditions
                if (characteristics.get(LENS_FACING) == LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    return;
                }

                final List<Size> outputSizes = Arrays.asList(map.getOutputSizes(IMAGE_FORMAT));
                final Size largest = Collections.max(outputSizes, new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth() / 4, largest.getHeight() / 4, IMAGE_FORMAT, 2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, getBackgroundHandler());
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);

                final int orientation = mResources.getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);

        if (mCameraId == null) {
            return;
        }

        configureTransform(width, height);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            //noinspection MissingPermission
            mCameraManager.openCamera(mCameraId, mStateCallback, getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(LOG_TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Log.e(LOG_TAG, "Preview Width: " + mPreviewSize.getWidth() + ", Preview Height: " + mPreviewSize.getHeight());

            final int width = mPreviewSize.getWidth();
            final int height = mPreviewSize.getHeight();

            Surface surface = new Surface(texture);
            Surface mImageSurface = mImageReader.getSurface();
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mImageSurface);
            mPreviewRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(mImageSurface, surface), new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (mCameraDevice == null) {
                                return;
                            }
                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                final int centerX = width / 2;
                                final int centerY = height / 2;
                                final int meterWidth = 2 * width / 3;
                                final int meterHeight = 2 * height / 3;
                                final MeteringRectangle focusArea = new MeteringRectangle(centerX - meterWidth / 2, centerY - meterHeight / 2, meterWidth, meterHeight, 1);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{focusArea});
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, getBackgroundHandler());
                            } catch (CameraAccessException e) {
                                throw new IllegalStateException("Could not access the camera.", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            throw new IllegalStateException("Configure failed...");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (mTextureView == null || mPreviewSize == null) {
            return;
        }

        final int rotation = mWindowManager.getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            Log.i(LOG_TAG, "transforming...");
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}
