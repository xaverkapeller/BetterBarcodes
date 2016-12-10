package com.github.wrdlbrnft.betterbarcodes.reader.permissions;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 25/01/16
 */
@KeepClass
@KeepClassMembers
public interface PermissionRequest {

    void start(Activity activity);
    void continueAfterRationale(Activity activity);

    void start(Fragment fragment);
    void continueAfterRationale(Fragment fragment);

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
}
