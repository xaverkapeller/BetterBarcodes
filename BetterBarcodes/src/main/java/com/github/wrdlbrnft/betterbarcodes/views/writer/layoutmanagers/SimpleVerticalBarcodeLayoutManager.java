package com.github.wrdlbrnft.betterbarcodes.views.writer.layoutmanagers;

import android.util.TypedValue;
import android.view.View;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 02/12/2016
 */
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class SimpleVerticalBarcodeLayoutManager extends AbsBarcodeLayoutManager {

    private static final int OFF_CENTER_RETAIN_COUNT = 1;

    @Override
    public int getOffCenterRetainCount() {
        return OFF_CENTER_RETAIN_COUNT;
    }

    @Override
    public void onTransformBarcode(ContainerInfo container, View view, float progress) {
        view.setTranslationY(container.getHeight() * -progress * 0.9f);
        view.setRotationX(progress * -90.0f * 0.9f);
    }

    @Override
    public void onTransformDescription(ContainerInfo container, View view, float progress) {
        view.setTranslationY(container.getHeight() * -progress);
    }

    @Override
    public float calculateProgress(float horizontalProgress, float verticalProgress) {
        return verticalProgress;
    }

    @Override
    public void onPrepareBarcodeContainer(View barcodes) {
        final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16.0f, barcodes.getResources().getDisplayMetrics());
        barcodes.setPadding(padding, padding, padding, padding);
    }

    @Override
    public void onPrepareDescriptionContainer(View descriptions) {
        final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0f, descriptions.getResources().getDisplayMetrics());
        descriptions.setPadding(padding, padding, padding, padding);
    }
}
