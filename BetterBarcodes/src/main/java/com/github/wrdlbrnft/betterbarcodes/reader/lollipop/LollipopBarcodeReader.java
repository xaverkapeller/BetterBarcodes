package com.github.wrdlbrnft.betterbarcodes.reader.lollipop;

import android.annotation.SuppressLint;
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
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeImageDecoder;
import com.github.wrdlbrnft.betterbarcodes.views.AspectRatioTextureView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 25/01/16
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopBarcodeReader extends BaseBarcodeReader {

    private static final int IMAGE_FORMAT = ImageFormat.YUV_420_888;
    private static final String LOG_TAG = LollipopBarcodeReader.class.getSimpleName();

    private static final int MAX_WIDTH = 1280;

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

    private final Semaphore mSemaphore = new Semaphore(3);

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = reader -> {
        final Image image = reader.acquireLatestImage();

        if(image == null) {
            return;
        }

        final Image.Plane[] planes = image.getPlanes();
        if (planes == null) {
            image.close();
            return;
        }

        if (mSemaphore.tryAcquire()) {
            submitImageData(image)
                    .onResult(result -> mSemaphore.release())
                    .onCanceled(mSemaphore::release)
                    .onError(throwable -> mSemaphore.release());
        } else {
            image.close();
        }
    };

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

    private Size mOutputSize;
    private CameraInfo mCameraInfo;
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
                if (characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING) == android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map = characteristics.get(android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    return;
                }

                final int orientation = mResources.getConfiguration().orientation;
                mOutputSize = chooseOptimalOutputSize(map, orientation, width, height);
                final int outputWidth = mOutputSize.getWidth();
                final int outputHeight = mOutputSize.getHeight();

                mImageReader = ImageReader.newInstance(width, height, IMAGE_FORMAT, 6);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, getCameraHandler());

                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(outputWidth, outputHeight);
                } else {
                    mTextureView.setAspectRatio(outputHeight, outputWidth);
                }

                mCameraInfo = new CameraInfoImpl(characteristics);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    @SuppressWarnings("ConstantConditions")
    private static class CameraInfoImpl implements CameraInfo {

        private final CameraCharacteristics mCameraCharacteristics;

        private CameraInfoImpl(CameraCharacteristics cameraCharacteristics) {
            mCameraCharacteristics = cameraCharacteristics;
        }

        @Override
        public int getSensorOrientation() {
            return mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        }

        @Override
        public boolean isFrontFacing() {
            return mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        }

        @Override
        public boolean isBackFacing() {
            return mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK;
        }
    }

    private Size chooseOptimalOutputSize(StreamConfigurationMap map, int orientation, int width, int height) {
        final float targetAspect = width > height
                ? (float) width / height
                : (float) height / width;

        final List<Size> outputSizes = Arrays.asList(map.getOutputSizes(IMAGE_FORMAT));
        Size outputSize = outputSizes.get(0);
        float outputAspect = (float) outputSize.getWidth() / outputSize.getHeight();
        for (Size candidateSize : outputSizes) {
            if (candidateSize.getWidth() > MAX_WIDTH) {
                continue;
            }

            final float candidateAspect = (float) candidateSize.getWidth() / candidateSize.getHeight();
            if (Math.abs(candidateAspect - targetAspect) < Math.abs(outputAspect - targetAspect)) {
                outputSize = candidateSize;
                outputAspect = candidateAspect;
            }
        }
        return outputSize;
    }

    @SuppressLint("MissingPermission")
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
            mCameraManager.openCamera(mCameraId, mStateCallback, getCameraHandler());
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

    private void createCameraPreviewSession() {
        try {
            final SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(mOutputSize.getWidth(), mOutputSize.getHeight());
            Log.e(LOG_TAG, "Preview Width: " + mOutputSize.getWidth() + ", Preview Height: " + mOutputSize.getHeight());

            final Surface surface = new Surface(texture);
            final Surface mImageSurface = mImageReader.getSurface();
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

                                final CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraDevice.getId());
                                final List<CaptureRequest.Key<?>> availableKeys = cameraCharacteristics.getAvailableCaptureRequestKeys();
                                if (availableKeys.contains(CaptureRequest.CONTROL_AF_MODE)) {
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                }

                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, getCameraHandler());
                            } catch (CameraAccessException e) {
                                throw new IllegalStateException("Could not access the camera.", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e(LOG_TAG, "Failed to configure capture session");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (mTextureView == null || mOutputSize == null) {
            return;
        }

        final int rotation = mWindowManager.getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mOutputSize.getHeight(), mOutputSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            Log.i(LOG_TAG, "transforming...");
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mOutputSize.getHeight(),
                    (float) viewWidth / mOutputSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }
}
