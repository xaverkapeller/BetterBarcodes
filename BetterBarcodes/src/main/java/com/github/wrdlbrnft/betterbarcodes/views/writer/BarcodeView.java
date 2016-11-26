package com.github.wrdlbrnft.betterbarcodes.views.writer;

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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;
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

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

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

    @IntDef({
            STATE_DISPLAY, STATE_DISPLAY_TOUCH, STATE_DISPLAY_SWIPE,
            STATE_SELECT, STATE_SELECT_TOUCH, STATE_SELECT_SWIPE
    })
    private @interface State {
    }

    @KeepClass
    @KeepClassMembers
    public interface LayoutManager {

        int STATE_DISPLAY = 0x01;
        int STATE_SELECT = 0x02;

        @IntDef({STATE_DISPLAY, STATE_SELECT})
        @interface State {
        }

        int getOffCenterRetainCount();
        boolean isSelectModeOnTapEnabled();
        boolean isSelectModeOnPressEnabled();
        void switchToSelectMode(View container);
        void switchToDisplayMode(View container);
        void onTransform(View view, float progress);
        float calculateProgress(float horizontalProgress, float verticalProgress);

        @State
        int getState();
    }

    public interface ViewPool<T extends View> {
        T claimView();
        void returnView(T view);
    }

    public interface Binder<V extends View, T> {
        Future<Bitmap> bind(V view, T data);
    }

    public static final LayoutManager DEFAULT_LAYOUT_MANAGER = new LinearLayoutManager(LinearLayoutManager.ORIENTATION_HORIZONTAL);

    private int[] mFormats = new int[]{BarcodeFormat.QR_CODE};
    private final LinkedList<ViewHolder> mViewHolders = new LinkedList<>();

    private ViewGroup mContainer;
    private String mText;

    @State
    private int mState = STATE_DISPLAY;

    private float mTouchStartX;
    private float mTouchStartY;
    private float mPosition = 0.0f;
    private float mTouchPosition = 0.0f;
    private long mTouchStartTime;
    private int mTouchSlop;

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
            final BarcodeWriter writer = BarcodeWriters.forFormat(info.format);
            if (TextUtils.isEmpty(info.text)) {
                return null;
            }
            return writer.write(info.text, info.width, info.height);
        }
    };

    private final Binder<ImageView, BarcodeInfo> mBarcodeBinder = (view, info) -> {
        final FutureTask<Bitmap> task = new FutureTask<>(() -> {
            final Bitmap bitmap = mCache.get(info);
            view.post(() -> view.setImageBitmap(bitmap));
        }, null);
        EXECUTOR.execute(task);
        return task;
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

    private LayoutManager mLayoutManager;

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
            mText = typedArray.getString(R.styleable.BarcodeView_text);
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
        layoutViews();
    }

    public void setText(String text) {
        mText = text;
        rebindViews();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContainer = (ViewGroup) findViewById(R.id.container);
        setLayoutManager(DEFAULT_LAYOUT_MANAGER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rebindViews();
    }

    public void setLayoutManager(@NonNull LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        post(this::layoutViews);
    }

    private void layoutViews() {
        for (ViewHolder holder : mViewHolders) {
            holder.unbind();
        }

        mViewHolders.clear();

        final int retainCount = mLayoutManager.getOffCenterRetainCount();
        final int viewCount = 2 * retainCount + 1;
        for (int i = 0; i < viewCount; i++) {
            final ViewHolder holder = new ViewHolder(mViewPool, mLayoutManager, mBarcodeBinder);
            final int index = i - retainCount;
            holder.bind(index, getBarcodeInfoForIndex(index));
            mViewHolders.add(holder);
        }
        post(() -> updatePosition(mPosition));
    }

    private void rebindViews() {
        if (mViewHolders.isEmpty()) {
            return;
        }

        final int retainCount = mLayoutManager.getOffCenterRetainCount();
        final int viewCount = 2 * retainCount + 1;
        for (int i = 0; i < viewCount; i++) {
            final ViewHolder holder = mViewHolders.get(i);
            final int index = holder.getIndex();
            holder.bind(index, getBarcodeInfoForIndex(index));
        }
    }

    private void updatePosition(float position) {
        mPosition = position;

        if (mViewHolders.peekLast().shouldRecycle(position)) {
            final int index = mViewHolders.peekFirst().getIndex() - 1;
            final ViewHolder holder = mViewHolders.removeLast();
            final BarcodeInfo info = getBarcodeInfoForIndex(index);
            holder.bind(index, info);
            mViewHolders.addFirst(holder);
        }

        if (mViewHolders.peekFirst().shouldRecycle(position)) {
            final int index = mViewHolders.peekLast().getIndex() + 1;
            final ViewHolder holder = mViewHolders.removeFirst();
            final BarcodeInfo info = getBarcodeInfoForIndex(index);
            holder.bind(index, info);
            mViewHolders.addLast(holder);
        }

        for (int i = 0, count = mViewHolders.size(); i < count; i++) {
            ViewHolder viewHolder = mViewHolders.get(i);
            viewHolder.updatePosition(position);
        }
    }

    @NonNull
    private BarcodeInfo getBarcodeInfoForIndex(int index) {
        final int format = getFormatForIndex(index);
        return new BarcodeInfo(format, mText != null ? mText : "", mContainer.getWidth(), mContainer.getHeight());
    }

    private int getFormatForIndex(int index) {
        final int formatIndex = index % mFormats.length;
        return mFormats[formatIndex < 0 ? formatIndex + mFormats.length : formatIndex];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final float x = event.getRawX();
        final float y = event.getRawY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (mFormats.length < 2 || mText == null) {
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

    @KeepClass
    @KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
    public static class SimpleVerticalLayoutManager extends AbsLayoutManager {

        private static final int OFF_CENTER_RETAIN_COUNT = 1;

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
            return verticalProgress;
        }
    }

    @KeepClass
    @KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
    public static class LinearLayoutManager extends AbsLayoutManager {

        public static final int ORIENTATION_VERTICAL = 0x01;
        public static final int ORIENTATION_HORIZONTAL = 0x02;

        private static final int OFF_CENTER_RETAIN_COUNT = 2;

        @IntDef({ORIENTATION_HORIZONTAL, ORIENTATION_VERTICAL})
        public @interface Orientation {
        }

        @Orientation
        private final int mOrientation;
        private Animator mAnimator;

        public LinearLayoutManager(@Orientation int orientation) {
            mOrientation = orientation;
        }

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
            switch (mOrientation) {

                case ORIENTATION_HORIZONTAL:
                    final float translationX = (float) view.getWidth() * -progress;
                    view.setTranslationX(translationX);
                    break;

                case ORIENTATION_VERTICAL:
                    final float translationY = (float) view.getHeight() * -progress * 1.2f;
                    view.setTranslationY(translationY);
                    break;

                default:
                    throw new IllegalStateException("Unknown orientation: " + mOrientation);
            }
        }

        @Override
        public float calculateProgress(float horizontalProgress, float verticalProgress) {
            switch (mOrientation) {

                case ORIENTATION_HORIZONTAL:
                    return getState() == STATE_SELECT
                            ? horizontalProgress * 2.0f
                            : horizontalProgress;

                case ORIENTATION_VERTICAL:
                    return getState() == STATE_SELECT
                            ? verticalProgress * 2.0f
                            : verticalProgress;

                default:
                    throw new IllegalStateException("Unknown orientation: " + mOrientation);
            }
        }
    }

    @KeepClass
    @KeepClassMembers
    public abstract static class AbsLayoutManager implements LayoutManager {

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

        @State
        @Override
        public final int getState() {
            return mState;
        }
    }
}