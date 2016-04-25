package com.github.wrdlbrnft.betterbarcodes.views.barcodeview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.R;
import com.github.wrdlbrnft.betterbarcodes.writer.BarcodeWriter;
import com.github.wrdlbrnft.betterbarcodes.writer.BarcodeWriters;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

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

    private static final int STATE_DISPLAY = 0x01;
    private static final int STATE_DISPLAY_TOUCH = 0x02;
    private static final int STATE_DISPLAY_SWIPE = 0x04;
    private static final int STATE_SELECT = 0x08;
    private static final int STATE_SELECT_TOUCH = 0x10;
    private static final int STATE_SELECT_SWIPE = 0x20;

    private static final String TAG = "BarcodeView";

    @IntDef({
            STATE_DISPLAY, STATE_DISPLAY_TOUCH, STATE_DISPLAY_SWIPE,
            STATE_SELECT, STATE_SELECT_TOUCH, STATE_SELECT_SWIPE
    })
    private @interface State {
    }

    @KeepClassMembers
    public interface LayoutManager {
        int getOffCenterRetainCount();
        boolean isSelectModeOnTapEnabled();
        boolean isSelectModeOnPressEnabled();
        void switchToSelectMode(View container);
        void switchToDisplayMode(View container);
        void onTransform(View view, float progress);
        float calculateProgress(float horizontalProgress, float verticalProgress);
    }

    public static final LayoutManager DEFAULT_LAYOUT_MANAGER = new HorizontalLayoutManager();

    private int[] mFormats = new int[]{BarcodeFormat.QR_CODE};
    private ViewGroup mContainer;
    private ImageView[] mImageViews = new ImageView[0];
    private String mToken;

    @State
    private int mState = STATE_DISPLAY;

    private float mTouchStartX;
    private float mTouchStartY;
    private float mPosition = 0.0f;
    private float mTouchPosition = 0.0f;
    private long mTouchStartTime;
    private int mTouchSlop;
    private boolean mGenerateBitmaps = false;

    private final LruCache<BarcodeInfo, Bitmap> mCache = new LruCache<BarcodeInfo, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8L)) {

        @Override
        protected int sizeOf(BarcodeInfo info, Bitmap value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return value.getAllocationByteCount();
            }

            return value.getByteCount();
        }

        @Override
        protected Bitmap create(BarcodeInfo info) {
            final BarcodeWriter writer = BarcodeWriters.forFormat(info.mFormat);
            return writer.write(info.mText, info.mWidth, info.mHeight);
        }
    };

    private final ViewPool<ImageView> mViewPool = new AbsViewPool<ImageView>() {
        @Override
        protected ImageView createView() {
            final ImageView view = new ImageView(getContext());
            view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(view);
            return view;
        }
    };

    private LayoutManager mLayoutManager = DEFAULT_LAYOUT_MANAGER;

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
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
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
                formats[count++] = format;
            }
        }
        final int[] result = new int[count];
        System.arraycopy(formats, 0, result, 0, count);
        return result;
    }

    public void setFormat(@BarcodeFormat int... formats) {
        mFormats = formats;
        queueGenerateBitmaps();
    }

    public void setToken(String token) {
        mToken = token;
        queueGenerateBitmaps();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContainer = (ViewGroup) findViewById(R.id.container);
        queueGenerateBitmaps();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        queueGenerateBitmaps();
    }

    public void setLayoutManager(@NonNull LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    private void queueGenerateBitmaps() {
        if (mGenerateBitmaps) {
            return;
        }
        mGenerateBitmaps = true;
        post(this::generateBitmaps);
    }

    private void generateBitmaps() {
        mGenerateBitmaps = false;
        if (mToken == null || mFormats.length == 0) {
            mPosition = 0.0f;
            for (ImageView imageView : mImageViews) {
                imageView.setImageBitmap(null);
            }
            return;
        }

        final int retainCount = mLayoutManager.getOffCenterRetainCount();
        final int requiredViewCount = retainCount * 2 + 1;
        final int currentViewCount = mImageViews.length;

        if (currentViewCount > requiredViewCount) {
            for (int i = currentViewCount - 1; i >= requiredViewCount - 1; i--) {
                final ImageView imageView = mImageViews[i];
                mViewPool.returnView(imageView);
            }
            final ImageView[] imageViews = new ImageView[requiredViewCount];
            System.arraycopy(mImageViews, 0, imageViews, 0, requiredViewCount);
            mImageViews = imageViews;
        } else if (currentViewCount < requiredViewCount) {
            final ImageView[] imageViews = new ImageView[requiredViewCount];
            System.arraycopy(mImageViews, 0, imageViews, 0, currentViewCount);
            for (int i = currentViewCount; i < requiredViewCount; i++) {
                imageViews[i] = mViewPool.claimView();
            }
            mImageViews = imageViews;
        }

        mContainer.post(() -> {
            updateBarcodes();
            updatePosition(mPosition);
        });
    }

    private void updateBarcodes() {
        for (int i = 0; i < mImageViews.length; i++) {
            final ImageView imageView = mImageViews[i];
            final int index = ((int) mPosition + i) % mFormats.length;
            final int format = mFormats[index];
            loadBarcodeForIndex(imageView, format);
        }
    }

    private void loadBarcodeForIndex(ImageView imageView, int format) {
        final BarcodeInfo viewInfo = (BarcodeInfo) imageView.getTag();
        final BarcodeInfo info = new BarcodeInfo(format, mToken, mContainer.getWidth(), mContainer.getHeight());
        if(info.equals(viewInfo)) {
            return;
        }
        imageView.setTag(info);

        EXECUTOR.execute(() -> {
            final Bitmap bitmap = mCache.get(info);
            imageView.post(() -> imageView.setImageBitmap(bitmap));
        });
    }

    private void updatePosition(float position) {
        mPosition = position;
        final float baseProgress = position % (float) mFormats.length;
        for (int i = 0, count = mImageViews.length; i < count; i++) {
            final ImageView imageView = mImageViews[i];
            final float progress = baseProgress + (float) (i - 2);
            mLayoutManager.onTransform(imageView, progress);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final float x = event.getRawX();
        final float y = event.getRawY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (mFormats.length < 2) {
                    return false;
                }
                mTouchStartX = x;
                mTouchStartY = y;
                mTouchStartTime = System.currentTimeMillis();
                mTouchPosition = mPosition;
                final boolean selectModeOnPress = mLayoutManager.isSelectModeOnPressEnabled();
                final boolean selectModeOnTap = mLayoutManager.isSelectModeOnTapEnabled();
                if (!selectModeOnPress && !selectModeOnTap) {
                    return true;
                }

                if (mState == STATE_SELECT) {
                    mState = STATE_SELECT_TOUCH;
                }

                if (mState == STATE_DISPLAY) {
                    mState = STATE_DISPLAY_TOUCH;
                    if (selectModeOnPress) {
                        mLayoutManager.switchToSelectMode(mContainer);
                    }
                }

                return true;

            case MotionEvent.ACTION_MOVE:
                final float horizontalProgress = (mTouchStartX - x) / getWidth();
                final float verticalProgress = (mTouchStartY - y) / getHeight();
                final float position = mLayoutManager.calculateProgress(horizontalProgress, verticalProgress) + mTouchPosition;
                updatePosition(position);

                if (mState == STATE_SELECT_TOUCH && isOutsideTapRange(x)) {
                    mState = STATE_SELECT_SWIPE;
                }

                if (mState == STATE_DISPLAY_TOUCH && isOutsideTapRange(x)) {
                    mState = STATE_DISPLAY_SWIPE;
                }

                return true;

            case MotionEvent.ACTION_UP:
                settleProgress();

                if (mState == STATE_DISPLAY_TOUCH) {
                    if (isInsideTapTime()) {
                        mState = STATE_SELECT;
                        if (mLayoutManager.isSelectModeOnTapEnabled() && !mLayoutManager.isSelectModeOnPressEnabled()) {
                            mLayoutManager.switchToSelectMode(mContainer);
                        }
                    } else {
                        mState = STATE_DISPLAY;
                        mLayoutManager.switchToDisplayMode(mContainer);
                    }
                }

                if (mState == STATE_DISPLAY_SWIPE) {
                    mState = STATE_DISPLAY;
                    mLayoutManager.switchToDisplayMode(mContainer);
                }

                if (mState == STATE_SELECT_TOUCH) {
                    if (isInsideTapTime()) {
                        mState = STATE_DISPLAY;
                        mLayoutManager.switchToDisplayMode(mContainer);
                    } else {
                        mState = STATE_SELECT;
                    }
                }

                if (mState == STATE_SELECT_SWIPE) {
                    mState = STATE_SELECT;
                }

                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    private boolean isInsideTapTime() {
        return (System.currentTimeMillis() - mTouchStartTime) < 200L;
    }

    private boolean isOutsideTapRange(float x) {
        return Math.abs(x - mTouchStartX) > mTouchSlop;
    }

    private void settleProgress() {
        final float finalPosition = Math.round(mPosition);
        final ValueAnimator animator = ValueAnimator.ofFloat(mPosition, finalPosition);
        animator.addUpdateListener(animation -> {
            final float position = (float) animation.getAnimatedValue();
            BarcodeView.this.updatePosition(position);
        });
        animator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);
        animator.start();
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

    @KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
    public static class SimpleVerticalLayoutManager extends AbsLayoutManager {

        private static final int OFF_CENTER_RETAIN_COUNT = 5;

        @Override
        public int getOffCenterRetainCount() {
            return OFF_CENTER_RETAIN_COUNT;
        }

        @Override
        public void onTransform(View view, float progress) {
            view.setTranslationY(view.getHeight() * -progress * 0.9f);
            view.setRotationX(progress * -90.0f * 0.9f);
        }

        @Override
        public float calculateProgress(float horizontalProgress, float verticalProgress) {
            return getState() == STATE_SELECT
                    ? verticalProgress * 2.0f
                    : verticalProgress;
        }
    }

    @KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
    public static class HorizontalLayoutManager extends AbsLayoutManager {

        public static final int OFF_CENTER_RETAIN_COUNT = 5;
        private Animator mAnimator;

        @Override
        public int getOffCenterRetainCount() {
            return OFF_CENTER_RETAIN_COUNT;
        }

        @Override
        public boolean isSelectModeOnTapEnabled() {
            return true;
        }

        @Override
        public boolean isSelectModeOnPressEnabled() {
            return false;
        }

        @Override
        public void onSwitchToSelectMode(View container) {
            if (mAnimator != null) {
                mAnimator.cancel();
            }

            mAnimator = ObjectAnimator.ofPropertyValuesHolder(container,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f));
            mAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);
            mAnimator.start();
        }

        @Override
        public void onSwitchToDisplayMode(View container) {
            if (mAnimator != null) {
                mAnimator.cancel();
            }

            mAnimator = ObjectAnimator.ofPropertyValuesHolder(container,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f));
            mAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);
            mAnimator.start();
        }

        @Override
        public void onTransform(View view, float progress) {
            final float translationX = (float) view.getWidth() * -progress;
            view.setTranslationX(translationX);
        }

        @Override
        public float calculateProgress(float horizontalProgress, float verticalProgress) {
            return getState() == STATE_SELECT
                    ? horizontalProgress * 2.0f
                    : horizontalProgress;
        }
    }

    @KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
    public abstract static class AbsLayoutManager implements LayoutManager {

        public static final int STATE_DISPLAY = 0x01;
        public static final int STATE_SELECT = 0x02;

        private int mState = STATE_DISPLAY;

        @Override
        public boolean isSelectModeOnTapEnabled() {
            return false;
        }

        @Override
        public boolean isSelectModeOnPressEnabled() {
            return false;
        }

        @Override
        public final void switchToSelectMode(View container) {
            mState = STATE_SELECT;
            onSwitchToSelectMode(container);
        }

        @Override
        public final void switchToDisplayMode(View container) {
            mState = STATE_DISPLAY;
            onSwitchToDisplayMode(container);
        }

        protected void onSwitchToDisplayMode(View container) {

        }

        protected void onSwitchToSelectMode(View container) {

        }

        public int getState() {
            return mState;
        }
    }

    private static class BarcodeInfo {
        private final int mFormat;
        private final String mText;
        private final int mWidth;
        private final int mHeight;

        private BarcodeInfo(int format, String text, int width, int height) {
            mFormat = format;
            mText = text;
            mWidth = width;
            mHeight = height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BarcodeInfo that = (BarcodeInfo) o;

            if (mFormat != that.mFormat) return false;
            if (mWidth != that.mWidth) return false;
            if (mHeight != that.mHeight) return false;
            return mText != null ? mText.equals(that.mText) : that.mText == null;

        }

        @Override
        public int hashCode() {
            int result = mFormat;
            result = 31 * result + (mText != null ? mText.hashCode() : 0);
            result = 31 * result + mWidth;
            result = 31 * result + mHeight;
            return result;
        }
    }
}
