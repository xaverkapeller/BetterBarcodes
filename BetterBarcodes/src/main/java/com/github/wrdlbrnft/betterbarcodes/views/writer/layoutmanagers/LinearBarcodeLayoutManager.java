package com.github.wrdlbrnft.betterbarcodes.views.writer.layoutmanagers;

import android.support.annotation.IntDef;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 02/12/2016
 */
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class LinearBarcodeLayoutManager extends AbsBarcodeLayoutManager {

    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    public static final int ORIENTATION_VERTICAL = 0x01;
    public static final int ORIENTATION_HORIZONTAL = 0x02;

    private static final int OFF_CENTER_RETAIN_COUNT = 2;

    @IntDef({ORIENTATION_HORIZONTAL, ORIENTATION_VERTICAL})
    public @interface Orientation {
    }

    @Orientation
    private final int mOrientation;

    public LinearBarcodeLayoutManager(@Orientation int orientation) {
        mOrientation = orientation;
    }

    @Override
    public int getOffCenterRetainCount() {
        return OFF_CENTER_RETAIN_COUNT;
    }

    @Override
    public boolean isSelectModeOnTapEnabled() {
        return mOrientation == ORIENTATION_HORIZONTAL;
    }

    @Override
    public boolean isSelectModeOnPressEnabled() {
        return false;
    }

    @Override
    public void onSwitchToSelectMode(View barcodes, View descriptions) {
        barcodes.animate().scaleX(0.5f).scaleY(0.5f);
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            if (descriptions.getVisibility() != View.VISIBLE) {
                descriptions.setVisibility(View.VISIBLE);
                descriptions.setTranslationY(descriptions.getHeight());
            }
            descriptions.animate().translationY(0.0f);
        }
    }

    @Override
    public void onSwitchToDisplayMode(View barcodes, View descriptions) {
        barcodes.animate().scaleX(1.0f).scaleY(1.0f);
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            descriptions.animate().translationY(descriptions.getHeight());
        }
    }

    @Override
    public void onTransformBarcode(ContainerInfo container, View view, float progress) {
        switch (mOrientation) {

            case ORIENTATION_HORIZONTAL:
                final float translationX = (float) container.getWidth() * -progress;
                view.setTranslationX(translationX);
                break;

            case ORIENTATION_VERTICAL:
                final float translationY = (float) container.getHeight() * -progress * 1.2f;
                view.setTranslationY(translationY);
                break;

            default:
                throw new IllegalStateException("Unknown orientation: " + mOrientation);
        }
    }

    @Override
    public void onTransformDescription(ContainerInfo container, View view, float progress) {
        switch (mOrientation) {

            case ORIENTATION_HORIZONTAL:
                final float translationX = container.getWidth() * -progress;
                view.setTranslationX(translationX);
                break;

            case ORIENTATION_VERTICAL:
                final float translationY = container.getHeight() * -progress;
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

    @Override
    public void onPrepareDescriptionContainer(View descriptions) {
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16.0f, descriptions.getResources().getDisplayMetrics());
            descriptions.setPadding(padding, padding, padding, padding);
            descriptions.setVisibility(View.INVISIBLE);
        }
    }
}
