package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.zxing;

import android.annotation.TargetApi;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeImageDecoder;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeResult;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatUtils;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;
import com.github.wrdlbrnft.simpletasks.exceptions.TaskExecutionException;
import com.github.wrdlbrnft.simpletasks.runners.TaskRunner;
import com.github.wrdlbrnft.simpletasks.tasks.Task;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 01/04/16
 */

@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class ZXingBarcodeImageDecoder implements BarcodeImageDecoder {

    private final TaskRunner mRunner;
    private Reader mReader;

    public ZXingBarcodeImageDecoder(TaskRunner runner) {
        mRunner = runner;
        mReader = new MultiFormatZXingReader(FormatUtils.split(BarcodeFormat.QR_CODE));
    }

    @NonNull
    @Override
    public Task<List<BarcodeResult>> decode(@Orientation int orientation, byte[] data, int width, int height) {
        return mRunner.queue(() -> {
            try {
                return tryDecodeBarcode(orientation, data, width, height);
            } catch (NotFoundException | ChecksumException | FormatException ignored) {
                return Collections.emptyList();
            } finally {
                mReader.reset();
            }
        });
    }

    @Override
    public void setFormat(int... format) {
        mReader = new MultiFormatZXingReader(FormatUtils.split(format));
    }

    @NonNull
    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public Task<List<BarcodeResult>> decode(@Orientation int orientation, Image image) {
        return mRunner.queue(() -> {
            try {
                final Image.Plane[] planes = image.getPlanes();
                final int count;
                final byte[][] planeBuffer;
                final int[][] strideBuffer;
                count = planes.length;
                planeBuffer = new byte[count][];
                strideBuffer = new int[count][];
                for (int i = 0; i < count; i++) {
                    final Image.Plane plane = planes[i];
                    if (plane == null) {
                        throw new TaskExecutionException("Found MediaImage without any valid image planes. Ignoring it...");
                    }
                    final ByteBuffer buffer = plane.getBuffer();
                    planeBuffer[i] = new byte[buffer.remaining()];
                    strideBuffer[i] = new int[]{
                            plane.getPixelStride(),
                            plane.getRowStride()
                    };
                    buffer.get(planeBuffer[i]);
                }
                for (int i = 0; i < count; i++) {
                    final byte[] planeData = planeBuffer[i];
                    final int[] strideData = strideBuffer[i];
                    final int rowStride = strideData[1];
                    try {
                        return tryDecodeBarcode(orientation, planeData, rowStride, planeData.length / rowStride);
                    } catch (NotFoundException | ChecksumException | FormatException ignored) {

                    }
                }
                return Collections.emptyList();
            } finally {
                image.close();
            }
        });
    }

    @NonNull
    private synchronized List<BarcodeResult> tryDecodeBarcode(@Orientation int orientation, byte[] data, int width, int height) throws NotFoundException, ChecksumException, FormatException {
        try {
            final PlanarYUVLuminanceSource luminanceSource = orientation == ORIENTATION_90 || orientation == ORIENTATION_270
                    ? PlanarYUVLuminanceSource.fromLandscape(data, width, height)
                    : PlanarYUVLuminanceSource.fromPortrait(data, width, height);
            final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
            final Result result = mReader.decode(bitmap);
            return Collections.singletonList(BarcodeResult.of(result.getText()));
        } finally {
            mReader.reset();
        }
    }
}
