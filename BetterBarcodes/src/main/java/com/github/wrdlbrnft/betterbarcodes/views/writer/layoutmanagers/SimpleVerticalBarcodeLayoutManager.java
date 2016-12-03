package com.github.wrdlbrnft.betterbarcodes.views.writer.layoutmanagers;

import android.view.View;

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
}
