package com.github.wrdlbrnft.betterbarcodes.writer;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.github.wrdlbrnft.betterbarcodes.exceptions.BarcodeWriterException;
import com.github.wrdlbrnft.betterbarcodes.utils.MapBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Map;

/**
 * Created by kapeller on 30/03/16.
 */
abstract class AbsZXingBarcodeWriter implements BarcodeWriter {

    private static final MultiFormatWriter MULTI_FORMAT_WRITER = new MultiFormatWriter();
    private static final Map<EncodeHintType, ?> HINT_MAP = new MapBuilder<EncodeHintType, Object>()
            .put(EncodeHintType.MARGIN, 0)
            .build();

    @NonNull
    public static int[] getBarcodePixels(BarcodeFormat format, String token, int imageWidth, int imageHeight) {
        try {
            return tryGetBarcodePixels(format, token, imageWidth, imageHeight);
        } catch (WriterException e) {
            throw new BarcodeWriterException("Failed to create barcode image.", e);
        }
    }

    @NonNull
    private static int[] tryGetBarcodePixels(BarcodeFormat format, String token, int imageWidth, int imageHeight) throws WriterException {
        final BitMatrix bitMatrix = MULTI_FORMAT_WRITER.encode(token, format, imageWidth, imageHeight, HINT_MAP);
        final int width = bitMatrix.getWidth();
        final int height = bitMatrix.getHeight();
        final int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        return pixels;
    }
}
