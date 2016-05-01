package com.github.wrdlbrnft.betterbarcodes.views.writer;

import android.widget.ImageView;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 01/05/16
 */
class ViewHolder {
    public static final int STATE_UNBOUND = 0x01;
    public static final int STATE_BOUND = 0x02;

    private final BarcodeView.ViewPool<ImageView> mViewPool;
    private final BarcodeView.LayoutManager mLayoutManager;
    private final BarcodeView.Binder<ImageView, BarcodeInfo> mBinder;

    private int mIndex;
    private BarcodeInfo mInfo;
    private ImageView mImageView;
    private int mState = STATE_UNBOUND;

    ViewHolder(BarcodeView.ViewPool<ImageView> viewPool, BarcodeView.LayoutManager layoutManager, BarcodeView.Binder<ImageView, BarcodeInfo> binder) {
        mViewPool = viewPool;
        mLayoutManager = layoutManager;
        mBinder = binder;
    }

    public void updatePosition(float position) {
        final float progress = position + mIndex;
        mLayoutManager.onTransform(mImageView, progress);
    }

    public boolean shouldRecycle(float position) {
        final float offset = Math.abs(position + mIndex);
        return offset >= mLayoutManager.getOffCenterRetainCount() + 1;
    }

    public void bind(int index, BarcodeInfo info) {
        if (mState == STATE_BOUND) {
            unbind();
        }
        mState = STATE_BOUND;

        mIndex = index;
        mInfo = info;
        if (mImageView == null) {
            mImageView = mViewPool.claimView();
        }
        bindBarcode(mImageView, info);
    }

    public void unbind() {
        if (mState == STATE_UNBOUND) {
            return;
        }
        mState = STATE_UNBOUND;

        mViewPool.returnView(mImageView);

        mInfo = null;
        mImageView = null;
    }

    private void bindBarcode(ImageView imageView, BarcodeInfo info) {
        final BarcodeInfo viewInfo = (BarcodeInfo) imageView.getTag();
        if (info.equals(viewInfo)) {
            return;
        }
        imageView.setTag(info);
        mImageView.setImageBitmap(null);
        mBinder.bind(imageView, info);
    }

    public int getIndex() {
        return mIndex;
    }
}
