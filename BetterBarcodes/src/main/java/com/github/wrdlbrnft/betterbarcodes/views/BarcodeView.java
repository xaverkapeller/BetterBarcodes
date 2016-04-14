package com.github.wrdlbrnft.betterbarcodes.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.R;
import com.github.wrdlbrnft.betterbarcodes.writer.BarcodeWriter;
import com.github.wrdlbrnft.betterbarcodes.writer.BarcodeWriters;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by kapeller on 05/02/16.
 */
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class BarcodeView extends FrameLayout {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    public interface Transformation {
        void transform(View view, float progress);
    }

    public static final Transformation DEFAULT_TRANSFORMATION = (view, progress) -> {
        final float angle = progress * -90.0f;
        view.setRotationY(angle);
    };

    private ImageView mImageView;
    private TextView mTopView;
    private TextView mBottomView;
    private AnimatorSet mAnimator;

    private int[] mFormats = new int[]{BarcodeFormat.QR_CODE};
    private String mToken;

    private float mPosition;
    private Bitmap[] mBitmaps;
    private int mBitmapIndex;

    private float mTouchStartX;
    private float mTouchStartPosition;
    private Transformation mTransformation = DEFAULT_TRANSFORMATION;

    public BarcodeView(Context context) {
        super(context);
        init(context);
    }

    public BarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        readAttributes(context, attrs);
    }

    public BarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        readAttributes(context, attrs);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_barcode, this);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarcodeView);
        try {
            mFormats = readFormatAttribute(typedArray);
            mToken = typedArray.getString(R.styleable.BarcodeView_token);
        } finally {
            typedArray.recycle();
        }
    }

    private int[] readFormatAttribute(TypedArray typedArray) {
        final int formatFlags = typedArray.getInt(R.styleable.BarcodeView_format, BarcodeFormat.QR_CODE);
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

    public void setFormat(@BarcodeFormat int... formats) {
        mFormats = formats;
        post(this::generateBitmaps);
    }

    public void setToken(String token) {
        mToken = token;
        post(this::generateBitmaps);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.image_view);
        mTopView = (TextView) findViewById(R.id.top_view);
        mBottomView = (TextView) findViewById(R.id.bottom_view);

        post(() -> mTopView.setTranslationY(-mTopView.getHeight()));
        post(() -> mBottomView.setTranslationY(mBottomView.getHeight()));
        post(this::generateBitmaps);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        post(this::generateBitmaps);
    }

    public void setTransformation(@NonNull Transformation transformation) {
        mTransformation = transformation;
    }

    private void generateBitmaps() {
        if (mToken == null) {
            mPosition = 0.0f;
            mBitmaps = null;
            mImageView.setImageBitmap(null);
            return;
        }

        EXECUTOR.execute(new BarcodeImageRunnable(
                mFormats,
                mToken,
                getMeasuredWidth(),
                getMeasuredHeight()
        ));
    }

    private void updateUi(Bitmap[] bitmaps) {
        mBitmaps = bitmaps;
        if (bitmaps == null || bitmaps.length == 0) {
            return;
        }

        mImageView.setImageBitmap(bitmaps[0]);
        mPosition = 0.0f;
    }

    private void updatePosition(float position) {
        mPosition = position;
        final float step = (float) Math.floor(position);
        final int stepIndex = Math.round(step);
        final float progress = position - step;

        final float transformProgress = progress <= 0.5f
                ? progress * 2.0f
                : -(1.0f - progress) * 2.0f;

        if (progress >= 0.5f && mBitmapIndex != stepIndex + 1) {
            mBitmapIndex = Math.abs(stepIndex + 1);
        } else if (progress <= 0.5f && mBitmapIndex != stepIndex) {
            mBitmapIndex = Math.abs(stepIndex);
        }

        final int index = mBitmapIndex % mBitmaps.length;
        mImageView.setImageBitmap(mBitmaps[index]);
        mBottomView.setText(getNameForFormat(mFormats[index]));
        mTransformation.transform(mImageView, transformProgress);
    }

    @StringRes
    private static int getNameForFormat(@BarcodeFormat int format) {
        switch (format) {

            case BarcodeFormat.AZTEC:
                return R.string.barcode_name_aztec;

            case BarcodeFormat.CODABAR:
                return R.string.barcode_name_codabar;

            case BarcodeFormat.CODE_128:
                return R.string.barcode_name_code_128;

            case BarcodeFormat.CODE_39:
                return R.string.barcode_name_code_39;

            case BarcodeFormat.CODE_93:
                return R.string.barcode_name_code_93;

            case BarcodeFormat.DATA_MATRIX:
                return R.string.barcode_name_data_matrix;

            case BarcodeFormat.EAN_13:
                return R.string.barcode_name_ean_13;

            case BarcodeFormat.EAN_8:
                return R.string.barcode_name_ean_8;

            case BarcodeFormat.ITF:
                return R.string.barcode_name_itf;

            case BarcodeFormat.MAXICODE:
                return R.string.barcode_name_maxi_code;

            case BarcodeFormat.PDF_417:
                return R.string.barcode_name_pdf_417;

            case BarcodeFormat.QR_CODE:
                return R.string.barcode_name_qr_code;

            case BarcodeFormat.RSS_14:
                return R.string.barcode_name_rss_14;

            case BarcodeFormat.RSS_EXPANDED:
                return R.string.barcode_name_rss_expanded;

            case BarcodeFormat.UPC_A:
                return R.string.barcode_name_upc_a;

            case BarcodeFormat.UPC_EAN_EXTENSION:
                return R.string.barcode_name_upc_ean_extension;

            case BarcodeFormat.UPC_E:
                return R.string.barcode_name_upc_e;

            default:
                throw new IllegalStateException("Encountered unknown barcode format: " + format);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final float distanceScale = getWidth() / 2.0f;
        final float x = event.getRawX();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (mBitmaps == null || mBitmaps.length == 1) {
                    return false;
                }
                switchToSwipeLayout();
                mTouchStartX = x;
                mTouchStartPosition = mPosition;
                return true;

            case MotionEvent.ACTION_MOVE:
                final float distance = mTouchStartX - x;
                final float newPosition = mTouchStartPosition + distance / distanceScale;
                updatePosition(newPosition);
                return true;

            case MotionEvent.ACTION_UP:
                switchToDefaultLayout();
                final float finalPosition = Math.round(mPosition);
                final ValueAnimator animator = ValueAnimator.ofFloat(mPosition, finalPosition);
                animator.addUpdateListener(animation -> {
                    final float position = (float) animation.getAnimatedValue();
                    BarcodeView.this.updatePosition(position);
                });
                animator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);
                animator.start();
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    private void switchToSwipeLayout() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = new AnimatorSet();
        mAnimator.playTogether(
                ObjectAnimator.ofPropertyValuesHolder(mImageView,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f)
                ),
                ObjectAnimator.ofPropertyValuesHolder(mTopView,
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0.0f)
                ),
                ObjectAnimator.ofPropertyValuesHolder(mBottomView,
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0.0f)
                )
        );
        mAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);
        mAnimator.start();
    }

    private void switchToDefaultLayout() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = new AnimatorSet();
        mAnimator.playTogether(
                ObjectAnimator.ofPropertyValuesHolder(mImageView,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f)
                ),
                ObjectAnimator.ofPropertyValuesHolder(mTopView,
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -mTopView.getHeight())
                ),
                ObjectAnimator.ofPropertyValuesHolder(mBottomView,
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, mBottomView.getHeight())
                )
        );
        mAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);
        mAnimator.start();
    }

    private class BarcodeImageRunnable implements Runnable {

        @BarcodeFormat
        private final int[] mFormats;
        private final String mText;
        private final int mImageWidth;
        private final int mImageHeight;

        private BarcodeImageRunnable(@BarcodeFormat int[] formats, String text, int imageWidth, int imageHeight) {
            mFormats = formats;
            mText = text;
            mImageWidth = imageWidth;
            mImageHeight = imageHeight;
        }

        @Override
        public void run() {
            final List<Bitmap> bitmaps = new ArrayList<>();
            for (final int format : mFormats) {
                final BarcodeWriter writer = BarcodeWriters.forFormat(format);
                final Bitmap bitmap = writer.write(mText, mImageWidth, mImageHeight);
                bitmaps.add(bitmap);
            }
            final Bitmap[] array = bitmaps.toArray(new Bitmap[bitmaps.size()]);
            post(new ApplyImageRunnable(array));
        }
    }

    private class ApplyImageRunnable implements Runnable {

        private final Bitmap[] mBitmaps;

        private ApplyImageRunnable(Bitmap[] bitmaps) {
            mBitmaps = bitmaps;
        }

        @Override
        public void run() {
            updateUi(mBitmaps);
        }
    }
}
