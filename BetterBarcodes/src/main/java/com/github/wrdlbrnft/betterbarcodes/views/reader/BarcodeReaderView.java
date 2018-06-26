package com.github.wrdlbrnft.betterbarcodes.views.reader;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.R;
import com.github.wrdlbrnft.betterbarcodes.reader.BarcodeReader;
import com.github.wrdlbrnft.betterbarcodes.reader.BarcodeReaders;
import com.github.wrdlbrnft.betterbarcodes.reader.base.wrapper.BarcodeImageDecoder;
import com.github.wrdlbrnft.betterbarcodes.reader.permissions.PermissionHandler;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatUtils;
import com.github.wrdlbrnft.betterbarcodes.views.AspectRatioTextureView;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 25/01/16
 */
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class BarcodeReaderView extends FrameLayout {

    private BarcodeReader mBarcodeReader;

    public BarcodeReaderView(Context context) {
        super(context);
        init(context);
    }

    public BarcodeReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BarcodeReaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_barcode_reader, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final AspectRatioTextureView textureView = findViewById(R.id.texture);
        if (isInEditMode()) {
            return;
        }
        mBarcodeReader = BarcodeReaders.get(getContext(), textureView);
    }

    public void setCallback(BarcodeReader.Callback callback) {
        mBarcodeReader.setCallback(callback);
    }

    public void setDecoder(BarcodeImageDecoder decoder) {
        mBarcodeReader.setBarcodeImageDecoder(decoder);
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

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
    }

    private static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        int format;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.format = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.format);
        }
    }
}
