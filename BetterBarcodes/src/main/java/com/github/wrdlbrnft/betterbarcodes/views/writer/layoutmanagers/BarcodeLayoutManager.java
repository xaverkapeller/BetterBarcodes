package com.github.wrdlbrnft.betterbarcodes.views.writer.layoutmanagers;

import android.support.annotation.IntDef;
import android.view.View;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 02/12/2016
 */
@KeepClass
@KeepClassMembers
public interface BarcodeLayoutManager {

    int STATE_DISPLAY = 0x01;
    int STATE_SELECT = 0x02;

    @IntDef({STATE_DISPLAY, STATE_SELECT})
    @interface State {
    }

    @KeepClassMembers
    interface ContainerInfo {
        int getWidth();
        int getHeight();
    }

    int getOffCenterRetainCount();
    boolean isSelectModeOnTapEnabled();
    boolean isSelectModeOnPressEnabled();
    void switchToSelectMode(View barcodes, View descriptions);
    void switchToDisplayMode(View barcodes, View descriptions);
    void onTransformBarcode(ContainerInfo container, View view, float progress);
    void onTransformDescription(ContainerInfo container, View view, float progress);
    float calculateProgress(float horizontalProgress, float verticalProgress);

    @State
    int getState();
}
