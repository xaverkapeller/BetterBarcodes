package com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.zxing;

import android.util.Log;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.aztec.AztecReader;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.maxicode.MaxiCodeReader;
import com.google.zxing.oned.CodaBarReader;
import com.google.zxing.oned.Code128Reader;
import com.google.zxing.oned.Code39Reader;
import com.google.zxing.oned.Code93Reader;
import com.google.zxing.oned.EAN13Reader;
import com.google.zxing.oned.EAN8Reader;
import com.google.zxing.oned.ITFReader;
import com.google.zxing.oned.MultiFormatUPCEANReader;
import com.google.zxing.oned.UPCAReader;
import com.google.zxing.oned.UPCEReader;
import com.google.zxing.oned.rss.RSS14Reader;
import com.google.zxing.oned.rss.expanded.RSSExpandedReader;
import com.google.zxing.pdf417.PDF417Reader;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 09/12/16
 */
class MultiFormatZXingReader implements Reader {

    private static final String TAG = "MultiFormatZXingReader";

    private final List<Reader> mReaders;

    MultiFormatZXingReader(@BarcodeFormat int[] formats) {
        mReaders = createZXingReadersFromFormats(formats);
    }

    private static List<Reader> createZXingReadersFromFormats(@BarcodeFormat int[] formats) {
        Log.i(TAG, "Formats: " + Arrays.toString(formats));
        final List<Reader> readers = new ArrayList<>();

        for (@BarcodeFormat int format : formats) {
            final Reader reader = getReaderForFormat(format);
            if (reader == null) {
                continue;
            }
            readers.add(reader);
        }

        return readers;
    }

    private static Reader getReaderForFormat(@BarcodeFormat int format) {
        switch (format) {
            case BarcodeFormat.AZTEC:
                return new AztecReader();
            case BarcodeFormat.CODABAR:
                return new CodaBarReader();
            case BarcodeFormat.CODE_128:
                return new Code128Reader();
            case BarcodeFormat.CODE_39:
                return new Code39Reader();
            case BarcodeFormat.CODE_93:
                return new Code93Reader();
            case BarcodeFormat.DATA_MATRIX:
                return new DataMatrixReader();
            case BarcodeFormat.EAN_13:
                return new EAN13Reader();
            case BarcodeFormat.EAN_8:
                return new EAN8Reader();
            case BarcodeFormat.ITF:
                return new ITFReader();
            case BarcodeFormat.MAXICODE:
                return new MaxiCodeReader();
            case BarcodeFormat.PDF_417:
                return new PDF417Reader();
            case BarcodeFormat.QR_CODE:
                return new QRCodeReader();
            case BarcodeFormat.RSS_14:
                return new RSS14Reader();
            case BarcodeFormat.RSS_EXPANDED:
                return new RSSExpandedReader();
            case BarcodeFormat.UPC_A:
                return new UPCAReader();
            case BarcodeFormat.UPC_EAN_EXTENSION:
                return new MultiFormatUPCEANReader(null);
            case BarcodeFormat.UPC_E:
                return new UPCEReader();
            case BarcodeFormat.NONE:
                return null;
            default:
                throw new IllegalStateException("Unknown format: " + format);
        }
    }

    @Override
    public Result decode(BinaryBitmap image) throws NotFoundException {
        return decodeInternal(image);
    }

    @Override
    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        return decodeInternal(image);
    }

    @Override
    public void reset() {
        if (mReaders != null) {
            for (Reader reader : mReaders) {
                reader.reset();
            }
        }
    }

    private Result decodeInternal(BinaryBitmap image) throws NotFoundException {
        if (mReaders != null) {
            for (Reader reader : mReaders) {
                try {
                    return reader.decode(image, null);
                } catch (ReaderException re) {
                    // continue
                }
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
