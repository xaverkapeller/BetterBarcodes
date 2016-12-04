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
public abstract class AbsBarcodeLayoutManager implements BarcodeLayoutManager {

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
    public final void switchToSelectMode(View barcodes, View descriptions) {
        mState = STATE_SELECT;
        onSwitchToSelectMode(barcodes, descriptions);
    }

    @Override
    public final void switchToDisplayMode(View barcodes, View descriptions) {
        mState = STATE_DISPLAY;
        onSwitchToDisplayMode(barcodes, descriptions);
    }

    protected void onSwitchToDisplayMode(View barcodes, View descriptions) {

    }

    protected void onSwitchToSelectMode(View barcodes, View descriptions) {

    }

    @Override
    public void onPrepareBarcodeContainer(View barcodes) {

    }

    @Override
    public void onPrepareDescriptionContainer(View descriptions) {

    }

    @Override
    public void onConfigureBarcodeView(View view) {

    }

    @Override
    public void onConfigureDescriptionView(View view) {

    }

    @State
    @Override
    public final int getState() {
        return mState;
    }
}
