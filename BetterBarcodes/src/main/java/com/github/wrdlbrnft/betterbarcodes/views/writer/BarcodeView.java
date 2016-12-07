package com.github.wrdlbrnft.betterbarcodes.views.writer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.wrdlbrnft.betterbarcodes.BarcodeFormat;
import com.github.wrdlbrnft.betterbarcodes.R;
import com.github.wrdlbrnft.betterbarcodes.utils.FormatUtils;
import com.github.wrdlbrnft.betterbarcodes.views.writer.layoutmanagers.BarcodeLayoutManager;
import com.github.wrdlbrnft.betterbarcodes.views.writer.layoutmanagers.HorizontalRotatingLayoutManager;
import com.github.wrdlbrnft.betterbarcodes.writer.BarcodeWriter;
import com.github.wrdlbrnft.betterbarcodes.writer.BarcodeWriters;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 05/02/16
 */
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class BarcodeView extends FrameLayout {

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

    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private static final BarcodeLayoutManager DEFAULT_LAYOUT_MANAGER = new HorizontalRotatingLayoutManager();

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

    private final LinkedList<ViewController> mViewControllers = new LinkedList<>();

    private interface ViewPool<T extends View> {
        T claimView();
        void returnView(T view);
    }

    private final ViewPool<ImageView> mBarcodeViewPool = new AbsViewPool<ImageView>() {
        @Override
        protected ImageView createView() {
            final ImageView view = (ImageView) mInflater.inflate(R.layout.view_barcode, mBarcodeContainer, false);
            mBarcodeContainer.addView(view);
            return view;
        }
    };

    private final ViewPool<TextView> mDescriptionViewPool = new AbsViewPool<TextView>() {
        @Override
        protected TextView createView() {
            final TextView view = (TextView) mInflater.inflate(R.layout.view_description, mDescriptionContainer, false);
            mDescriptionContainer.addView(view);
            return view;
        }
    };

    private final BarcodeLayoutManager.ContainerInfo mContainerInfo = new BarcodeLayoutManager.ContainerInfo() {
        @Override
        public int getWidth() {
            return mBarcodeContainer.getWidth();
        }

        @Override
        public int getHeight() {
            return mBarcodeContainer.getWidth();
        }
    };

    private int[] mFormats = new int[]{BarcodeFormat.QR_CODE};
    private ViewGroup mBarcodeContainer;
    private ViewGroup mDescriptionContainer;
    private LayoutInflater mInflater;
    private String mText;
    private float mTouchStartX;
    private float mTouchStartY;
    private float mPosition = 0.0f;
    private float mTouchPosition = 0.0f;
    private long mTouchStartTime;
    private int mTouchSlop;

    @State
    private int mState = STATE_DISPLAY;

    private BarcodeLayoutManager mLayoutManager;

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
        inflate(context, R.layout.layout_barcode_viewer, this);
        mInflater = LayoutInflater.from(context);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();

        setBackgroundColor(Color.WHITE);
        setClipChildren(false);
        setClipToPadding(false);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarcodeView);
        try {
            //noinspection WrongConstant
            mFormats = FormatUtils.split(typedArray.getInt(R.styleable.BarcodeView_format, BarcodeFormat.QR_CODE));
            mText = typedArray.getString(R.styleable.BarcodeView_text);
        } finally {
            typedArray.recycle();
        }
    }

    public void setFormat(@BarcodeFormat int... formats) {
        mFormats = FormatUtils.split(formats);
        rebindViews();
    }

    @BarcodeFormat
    public int getFormat() {
        return FormatUtils.combine(mFormats);
    }

    public void setText(String text) {
        mText = text;
        rebindViews();
    }

    public String getText() {
        return mText;
    }

    public void setLayoutManager(@NonNull BarcodeLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        post(this::layoutViews);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBarcodeContainer = (ViewGroup) findViewById(R.id.container);
        mDescriptionContainer = (ViewGroup) findViewById(R.id.description_container);
        setLayoutManager(DEFAULT_LAYOUT_MANAGER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rebindViews();
    }

    private void layoutViews() {
        for (ViewController controller : mViewControllers) {
            controller.unbind();
        }
        mViewControllers.clear();

        final int retainCount = mLayoutManager.getOffCenterRetainCount();
        final int viewCount = 2 * retainCount + 1;
        for (int i = 0; i < viewCount; i++) {
            final ViewController controller = new ViewController();
            final int index = i - retainCount;
            controller.bind(index, getBarcodeInfoForIndex(index));
            mViewControllers.add(controller);
        }
        post(() -> {
            mLayoutManager.onPrepareBarcodeContainer(mBarcodeContainer);
            mLayoutManager.onPrepareDescriptionContainer(mDescriptionContainer);
            updatePosition(mPosition);
        });
    }

    private void rebindViews() {
        if (mViewControllers.isEmpty()) {
            return;
        }

        final int retainCount = mLayoutManager.getOffCenterRetainCount();
        final int viewCount = 2 * retainCount + 1;
        for (int i = 0; i < viewCount; i++) {
            final ViewController controller = mViewControllers.get(i);
            final int index = controller.getIndex();
            controller.bind(index, getBarcodeInfoForIndex(index));
            controller.updatePosition(mPosition);
        }
    }

    private void updatePosition(float position) {
        mPosition = position;

        if (mViewControllers.peekLast().shouldRecycle(position)) {
            final int index = mViewControllers.peekFirst().getIndex() - 1;
            final ViewController holder = mViewControllers.removeLast();
            final BarcodeInfo info = getBarcodeInfoForIndex(index);
            holder.bind(index, info);
            mViewControllers.addFirst(holder);
        }

        if (mViewControllers.peekFirst().shouldRecycle(position)) {
            final int index = mViewControllers.peekLast().getIndex() + 1;
            final ViewController holder = mViewControllers.removeFirst();
            final BarcodeInfo info = getBarcodeInfoForIndex(index);
            holder.bind(index, info);
            mViewControllers.addLast(holder);
        }

        for (int i = 0, count = mViewControllers.size(); i < count; i++) {
            final ViewController viewController = mViewControllers.get(i);
            viewController.updatePosition(position);
        }
    }

    @NonNull
    private BarcodeInfo getBarcodeInfoForIndex(int index) {
        final int format = getFormatForIndex(index);
        return new BarcodeInfo(
                format,
                mText != null ? mText : "",
                mBarcodeContainer.getWidth(),
                mBarcodeContainer.getHeight()
        );
    }

    private int getFormatForIndex(int index) {
        if (mFormats.length == 0) {
            return BarcodeFormat.NONE;
        }

        final int formatIndex = index % mFormats.length;
        return mFormats[formatIndex < 0 ? formatIndex + mFormats.length : formatIndex];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final float x = event.getRawX();
        final float y = event.getRawY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (mFormats.length < 2 || TextUtils.isEmpty(mText)) {
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
                        mLayoutManager.switchToSelectMode(mBarcodeContainer, mDescriptionContainer);
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
                    if (mLayoutManager.isSelectModeOnTapEnabled() && isInsideTapTime()) {
                        mState = STATE_SELECT;
                        if (!mLayoutManager.isSelectModeOnPressEnabled()) {
                            mLayoutManager.switchToSelectMode(mBarcodeContainer, mDescriptionContainer);
                        }
                    } else {
                        mState = STATE_DISPLAY;
                        mLayoutManager.switchToDisplayMode(mBarcodeContainer, mDescriptionContainer);
                    }
                }

                if (mState == STATE_DISPLAY_SWIPE) {
                    mState = STATE_DISPLAY;
                    mLayoutManager.switchToDisplayMode(mBarcodeContainer, mDescriptionContainer);
                }

                if (mState == STATE_SELECT_TOUCH) {
                    if (isInsideTapTime()) {
                        mState = STATE_DISPLAY;
                        mLayoutManager.switchToDisplayMode(mBarcodeContainer, mDescriptionContainer);
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


    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.formats = mFormats;
        savedState.text = mText;
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
        mFormats = savedState.formats;
        mText = savedState.text;
    }

    private class ViewController {

        static final int STATE_UNBOUND = 0x01;
        static final int STATE_BOUND = 0x02;

        private int mIndex;
        private ImageView mBarcodeView;
        private TextView mDescriptionView;
        private int mState = STATE_UNBOUND;

        void updatePosition(float position) {
            final float progress = position + mIndex;
            mLayoutManager.onTransformBarcode(mContainerInfo, mBarcodeView, progress);
            mLayoutManager.onTransformDescription(mContainerInfo, mDescriptionView, progress);
        }

        boolean shouldRecycle(float position) {
            final float offset = Math.abs(position + mIndex);
            return offset >= mLayoutManager.getOffCenterRetainCount() + 1;
        }

        void bind(int index, BarcodeInfo info) {
            if (mState == STATE_BOUND) {
                unbind();
            }
            mState = STATE_BOUND;

            mIndex = index;
            if (mBarcodeView == null) {
                mBarcodeView = mBarcodeViewPool.claimView();
                mLayoutManager.onConfigureBarcodeView(mBarcodeView);
            }
            bindBarcode(mBarcodeView, info);

            if (mDescriptionView == null) {
                mDescriptionView = mDescriptionViewPool.claimView();
                mLayoutManager.onConfigureDescriptionView(mDescriptionView);
            }
            mDescriptionView.setText(FormatUtils.getNameForFormat(info.format));
        }

        private void bindBarcode(ImageView imageView, BarcodeInfo info) {
            final BarcodeInfo viewInfo = (BarcodeInfo) imageView.getTag();
            if (info.equals(viewInfo)) {
                return;
            }
            imageView.setTag(info);
            imageView.setImageBitmap(mCache.get(info));
        }

        void unbind() {
            if (mState == STATE_UNBOUND) {
                return;
            }
            mState = STATE_UNBOUND;

            mBarcodeViewPool.returnView(mBarcodeView);
            mBarcodeView = null;

            mDescriptionViewPool.returnView(mDescriptionView);
            mDescriptionView = null;
        }

        int getIndex() {
            return mIndex;
        }
    }

    private abstract static class AbsViewPool<T extends View> implements ViewPool<T> {

        private final Queue<T> mViewQueue = new ArrayDeque<>();

        @Override
        public final T claimView() {
            final T view = mViewQueue.poll();
            if (view != null) {
                view.setVisibility(VISIBLE);
                return view;
            }

            return createView();
        }

        @Override
        public final void returnView(T view) {
            view.setVisibility(GONE);
            mViewQueue.add(view);
        }

        protected abstract T createView();
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

        int[] formats;
        String text;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            final int formatCount = in.readInt();
            this.formats = new int[formatCount];
            in.readIntArray(this.formats);
            this.text = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.formats.length);
            out.writeIntArray(this.formats);
            out.writeString(this.text);
        }
    }
}
