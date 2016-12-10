package com.github.wrdlbrnft.betterbarcodes.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 20/01/16
 */
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class AspectRatioTextureView extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AspectRatioTextureView(Context context) {
        super(context);
    }

    public AspectRatioTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mRatioWidth == 0 || mRatioHeight == 0) {
            setMeasuredDimension(width, height);
        } else {
            final int scaledWidth = height * mRatioWidth / mRatioHeight;
            if (width > scaledWidth) {
                final int scaledHeight = width * mRatioHeight / mRatioWidth;
                setMeasuredDimension(width, scaledHeight);
            } else {
                setMeasuredDimension(scaledWidth, height);
            }
        }
    }
}
