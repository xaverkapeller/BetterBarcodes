package com.github.wrdlbrnft.betterbarcodes.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.R;
import com.github.wrdlbrnft.betterbarcodes.reader.BarcodeReader;
import com.github.wrdlbrnft.betterbarcodes.reader.BarcodeReaders;
import com.github.wrdlbrnft.betterbarcodes.reader.permissions.PermissionHandler;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

/**
 * Created by kapeller on 25/01/16.
 */
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class BarcodeReaderView extends FrameLayout {

    private BarcodeReader mBarcodeReader;
    private int[] mFormats = new int[]{BarcodeFormat.QR_CODE};

    public BarcodeReaderView(Context context) {
        super(context);
        init(context);
    }

    public BarcodeReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        readAttributes(context, attrs);
    }

    public BarcodeReaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        readAttributes(context, attrs);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_barcode_reader, this);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarcodeReaderView);
        try {
            mFormats = readFormats(typedArray);
        } finally {
            typedArray.recycle();
        }
    }

    private int[] readFormats(TypedArray typedArray) {
        final int formatFlags = typedArray.getInt(R.styleable.BarcodeReaderView_format, BarcodeFormat.QR_CODE);
        int count = 0;
        final int[] formats = new int[BarcodeFormat.ALL_FORMATS.length];
        for (int i = 0; i < BarcodeFormat.ALL_FORMATS.length; i++) {
            final int format = BarcodeFormat.ALL_FORMATS[i];
            if ((formatFlags & format) > 0) {
                formats[i] = format;
                count++;
            }
        }
        final int[] result = new int[count];
        System.arraycopy(formats, 0, result, 0, count);
        return result;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final AspectRatioTextureView textureView = (AspectRatioTextureView) findViewById(R.id.texture);
        mBarcodeReader = BarcodeReaders.get(getContext(), textureView);
        mBarcodeReader.setFormat(mFormats);
    }

    public void setCallback(BarcodeReader.Callback callback) {
        mBarcodeReader.setCallback(callback);
    }

    public void setFormat(@BarcodeFormat int... format) {
        mBarcodeReader.setFormat(format);
    }

    public void setCameraPermissionHandler(PermissionHandler handler) {
        mBarcodeReader.setCameraPermissionHandler(handler);
    }

    public void startPreview() {
        mBarcodeReader.startPreview();
    }

    public void startScanning() {
        mBarcodeReader.startScanning();
    }

    public void stopScanning() {
        mBarcodeReader.stopScanning();
    }

    public void stopPreview() {
        mBarcodeReader.stopPreview();
    }

    public void start() {
        mBarcodeReader.startPreview();
        mBarcodeReader.startScanning();
    }

    public void stop() {
        mBarcodeReader.stopPreview();
        mBarcodeReader.stopScanning();
    }
}
