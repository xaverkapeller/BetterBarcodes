package com.github.wrdlbrnft.betterbarcodes.reader;

import android.content.Context;
import android.os.Build;

import com.github.wrdlbrnft.betterbarcodes.reader.icecreamsandwich.IceCreamSandwichBarcodeReader;
import com.github.wrdlbrnft.betterbarcodes.reader.lollipop.LollipopBarcodeReader;
import com.github.wrdlbrnft.betterbarcodes.views.AspectRatioTextureView;
import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

import java.util.Arrays;
import java.util.List;

/**
 * Created with Android Studio<br>
 * User: kapeller<br>
 * Date: 13/04/16
 */
@KeepClass
@KeepClassMembers
public class BarcodeReaders {
    
    private final List<String> CAMERA_2_DEVICES = Arrays.asList(
            "nexus 4", "nexus 5", "nexus 5x", "nexus 6", "nexus 6p", "nexus 7", "nexus 10",
            "gt-i9300", "samsung-sm-g925a", "samsung-sm-g935a", "samsung-sm-t817a", "sm-g900h",
            "lgus991", "lg-h810", "xt1058", "aquaris e5", "c6602"
    );

    public static BarcodeReader get(Context context, AspectRatioTextureView view) {
        return CAMERA_2_DEVICES.contains(Build.MODEL.toLowerCase())
                ? new LollipopBarcodeReader(context, view)
                : new IceCreamSandwichBarcodeReader(context, view);
    }
}
