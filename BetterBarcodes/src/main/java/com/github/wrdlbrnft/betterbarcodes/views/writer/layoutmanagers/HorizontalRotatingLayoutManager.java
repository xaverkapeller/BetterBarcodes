package com.github.wrdlbrnft.betterbarcodes.views.writer.layoutmanagers;

import android.util.TypedValue;
import android.view.View;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 02/12/2016
 */
@KeepClass
@KeepClassMembers
public class HorizontalRotatingLayoutManager extends AbsBarcodeLayoutManager {

    private static final int OFF_CENTER_RETAIN_COUNT = 1;

    @Override
    public int getOffCenterRetainCount() {
        return OFF_CENTER_RETAIN_COUNT;
    }

    @Override
    public void onTransformBarcode(ContainerInfo container, View view, float progress) {
        final float rotateProgress = progress * 2.0f;
        view.setVisibility(Math.abs(rotateProgress) >= 1.0f ? View.GONE : View.VISIBLE);
        view.setRotationY(rotateProgress * -90.0f);
    }

    @Override
    public void onTransformDescription(ContainerInfo container, View view, float progress) {
        view.setTranslationX(container.getWidth() * -progress);
    }

    @Override
    public boolean isSelectModeOnPressEnabled() {
        return true;
    }

    @Override
    protected void onSwitchToSelectMode(View barcodes, View descriptions) {
        barcodes.animate().scaleX(0.5f).scaleY(0.5f);
        if (descriptions.getVisibility() != View.VISIBLE) {
            descriptions.setVisibility(View.VISIBLE);
            descriptions.setTranslationY(descriptions.getHeight());
        }
        descriptions.animate().translationY(0.0f);
    }

    @Override
    protected void onSwitchToDisplayMode(View barcodes, View descriptions) {
        barcodes.animate().scaleX(1.0f).scaleY(1.0f);
        descriptions.animate().translationY(descriptions.getHeight());
    }

    @Override
    public void onConfigureBarcodeView(View view) {
        view.setCameraDistance(8000.0f);
    }

    @Override
    public void onPrepareDescriptionContainer(View descriptions) {
        final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16.0f, descriptions.getResources().getDisplayMetrics());
        descriptions.setPadding(padding, padding, padding, padding);
        descriptions.setVisibility(View.INVISIBLE);
    }

    @Override
    public float calculateProgress(float horizontalProgress, float verticalProgress) {
        return horizontalProgress;
    }
}