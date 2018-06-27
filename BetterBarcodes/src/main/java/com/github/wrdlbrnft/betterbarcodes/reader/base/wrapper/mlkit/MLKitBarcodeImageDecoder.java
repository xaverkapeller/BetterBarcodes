package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.mlkit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeImageDecoder;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeResult;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatUtils;
import com.github.wrdlbrnft.simpletasks.runners.TaskRunner;
import com.github.wrdlbrnft.simpletasks.tasks.StubTask;
import com.github.wrdlbrnft.simpletasks.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kapeller on 26.06.18.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MLKitBarcodeImageDecoder implements BarcodeImageDecoder {

    private static final String TAG = "MLKitBarcodeImageDecode";

    private final TaskRunner mRunner;

    private FirebaseVisionBarcodeDetector mDetector;
    private int mFormat;

    public MLKitBarcodeImageDecoder(TaskRunner runner) {
        mRunner = runner;
        mFormat = FirebaseVisionBarcode.FORMAT_ALL_FORMATS;
        final FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(mFormat)
                .build();
        mDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
    }

    @Override
    public void setFormat(int... formats) {
        if (mDetector != null) {
            try {
                mDetector.close();
            } catch (IOException e) {
                Log.d(TAG, "Failed to close detector.", e);
            }
        }

        mFormat = reduceToFirebaseFormat(formats);
        final FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(mFormat)
                .build();
        mDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
    }

    private int reduceToFirebaseFormat(int... formats) {
        @SuppressLint("WrongConstant") final int[] splitFormats = FormatUtils.split(formats);
        int result = 0;
        for (int splitFormat : splitFormats) {
            result |= convertToFirebaseBarcodeType(splitFormat);
        }
        return result;
    }

    @NonNull
    @Override
    public Task<List<BarcodeResult>> decode(@Orientation int orientation, byte[] data, int width, int height) {
        return mRunner.queue(() -> {
            final FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                    .setWidth(width)
                    .setHeight(height)
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setRotation(convertToFirebaseRotation(orientation))
                    .build();
            final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromByteArray(data, metadata);
            final Task<List<BarcodeResult>> decoderTask = decode(firebaseVisionImage);
            return decoderTask.await();
        });
    }

    private Task<List<BarcodeResult>> decode(FirebaseVisionImage firebaseVisionImage) {
        if (mDetector == null) {
            return Task.withError(null);
        }

        final StubTask<List<BarcodeResult>> stubTask = StubTask.create();
        mDetector.detectInImage(firebaseVisionImage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                final List<BarcodeResult> results = parseResult(task.getResult());
                stubTask.notifyResult(results);
            } else {
                stubTask.notifyError(null);
            }
        });

        return stubTask;
    }

    private List<BarcodeResult> parseResult(List<FirebaseVisionBarcode> barcodes) {
        final List<BarcodeResult> results = new ArrayList<>(barcodes.size());
        for (FirebaseVisionBarcode barcode : barcodes) {
            results.add(BarcodeResult.of(barcode.getRawValue()));
        }
        return results;
    }

    @NonNull
    @Override
    public Task<List<BarcodeResult>> decode(@Orientation int orientation, Image image) {
        return mRunner.queue(() -> {
            try {
                final int rotation = convertToFirebaseRotation(orientation);
                final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromMediaImage(image, rotation);
                final Task<List<BarcodeResult>> decoderTask = decode(firebaseVisionImage);
                return decoderTask.await();
            } finally {
                image.close();
            }
        });
    }

    private static int convertToFirebaseRotation(@Orientation int orientation) {
        switch (orientation) {
            case ORIENTATION_0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case ORIENTATION_90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case ORIENTATION_180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case ORIENTATION_270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                return FirebaseVisionImageMetadata.ROTATION_0;
        }
    }

    private static int convertToFirebaseBarcodeType(@BarcodeFormat int format) {
        switch (format) {
            case BarcodeFormat.AZTEC:
                return FirebaseVisionBarcode.FORMAT_AZTEC;
            case BarcodeFormat.CODABAR:
                return FirebaseVisionBarcode.FORMAT_CODABAR;
            case BarcodeFormat.CODE_39:
                return FirebaseVisionBarcode.FORMAT_CODE_39;
            case BarcodeFormat.CODE_93:
                return FirebaseVisionBarcode.FORMAT_CODE_93;
            case BarcodeFormat.CODE_128:
                return FirebaseVisionBarcode.FORMAT_CODE_128;
            case BarcodeFormat.DATA_MATRIX:
                return FirebaseVisionBarcode.FORMAT_DATA_MATRIX;
            case BarcodeFormat.EAN_8:
                return FirebaseVisionBarcode.FORMAT_EAN_8;
            case BarcodeFormat.EAN_13:
                return FirebaseVisionBarcode.FORMAT_EAN_13;
            case BarcodeFormat.ITF:
                return FirebaseVisionBarcode.FORMAT_ITF;
            case BarcodeFormat.MAXICODE:
                throw new IllegalArgumentException("Maxicode is unsupported by this detector.");
            case BarcodeFormat.PDF_417:
                return FirebaseVisionBarcode.FORMAT_PDF417;
            case BarcodeFormat.QR_CODE:
                return FirebaseVisionBarcode.FORMAT_QR_CODE;
            case BarcodeFormat.RSS_14:
                throw new IllegalArgumentException("RSS 14 is unsupported by this detector.");
            case BarcodeFormat.RSS_EXPANDED:
                throw new IllegalArgumentException("RSS EXPANDED is unsupported by this detector.");
            case BarcodeFormat.UPC_A:
                return FirebaseVisionBarcode.FORMAT_UPC_A;
            case BarcodeFormat.UPC_E:
                return FirebaseVisionBarcode.FORMAT_UPC_E;
            case BarcodeFormat.UPC_EAN_EXTENSION:
                throw new IllegalArgumentException("UPC EAN EXTENSION is unsupported by this detector.");
            case BarcodeFormat.NONE:
            default:
                return FirebaseVisionBarcode.FORMAT_ALL_FORMATS;
        }
    }
}
