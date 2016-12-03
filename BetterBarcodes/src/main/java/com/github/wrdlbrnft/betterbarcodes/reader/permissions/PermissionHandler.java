package com.github.wrdlbrnft.betterbarcodes.reader.permissions;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 28/01/16
 */
@KeepClass
@KeepClassMembers
public interface PermissionHandler {
    void onNewPermissionRequest(PermissionRequest request);
    boolean onShowRationale();
    void onPermissionGranted();
    void onPermissionDenied();

    @KeepClassMembers
    abstract class Adapter implements PermissionHandler {

        @Override
        public boolean onShowRationale() {
            return false;
        }

        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied() {

        }
    }
}
