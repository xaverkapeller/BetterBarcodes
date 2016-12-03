package com.github.wrdlbrnft.betterbarcodes;

import android.support.annotation.IntDef;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepClassMembers;

/**
 * Created with Android Studio
 * User: kapeller
 * Date: 25/01/16
 */
@KeepClass
@KeepClassMembers
@IntDef(value = {
        BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128, BarcodeFormat.AZTEC,
        BarcodeFormat.CODABAR, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
        BarcodeFormat.DATA_MATRIX, BarcodeFormat.EAN_8, BarcodeFormat.EAN_13,
        BarcodeFormat.ITF, BarcodeFormat.MAXICODE, BarcodeFormat.PDF_417,
        BarcodeFormat.RSS_14, BarcodeFormat.RSS_EXPANDED, BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E, BarcodeFormat.UPC_EAN_EXTENSION
})
public @interface BarcodeFormat {
    int QR_CODE = 0x00001;
    int CODE_128 = 0x00002;
    int AZTEC = 0x00004;
    int CODABAR = 0x00008;
    int CODE_39 = 0x00010;
    int CODE_93 = 0x00020;
    int DATA_MATRIX = 0x00040;
    int EAN_8 = 0x00080;
    int EAN_13 = 0x00100;
    int ITF = 0x00200;
    int MAXICODE = 0x00400;
    int PDF_417 = 0x00800;
    int RSS_14 = 0x01000;
    int RSS_EXPANDED = 0x02000;
    int UPC_A = 0x04000;
    int UPC_E = 0x08000;
    int UPC_EAN_EXTENSION = 0x10000;

    int[] ALL_FORMATS = new int[]{
            QR_CODE, CODE_128, AZTEC, CODABAR, CODE_39, CODE_93,
            DATA_MATRIX, EAN_8, EAN_13, ITF, MAXICODE, PDF_417,
            RSS_14, RSS_EXPANDED, UPC_A, UPC_E, UPC_EAN_EXTENSION
    };
}