# BetterBarcodes

The best way to read and display barcodes. 

- **Works on API level 14 and above**: That's 99% of all Android devices. BetterBarcodes takes care backwards compatibility so you don't have to worry about a thing!
- **Extensive support of barcode formats**: Read and display 1D or 2D barcodes in almost all common formats.
- **No lag, no loading times**: Uses the newest API and most efficient implementation to ensure the best possible performance and user experience.

[![Build Status](https://travis-ci.org/Wrdlbrnft/BetterBarcodes.svg?branch=master)](https://travis-ci.org/Wrdlbrnft/BetterBarcodes)
[![BCH compliance](https://bettercodehub.com/edge/badge/Wrdlbrnft/BetterBarcodes)](https://bettercodehub.com/)

## How do I add it to my project?

Just add this to the dependencies closure in your build.gradle:

```groovy
compile 'com.github.wrdlbrnft:better-barcodes:0.3.0.46'
```

## Example App

There is an example app maintained for BetterBarcodes you can find it on GitHub [**here**](https://github.com/Wrdlbrnft/BetterBarcodes-Example-App).

Or if you just want to test the library you can download the example app from the Play Store:

[![Get it on Google Play](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=com.github.wrdlbrnft.betterbarcodes.example.app)

## How do I use it?

BetterBarcodes includes two ready to use Views which you can add to your layout:

- `BarcodeView`: Displays a barcode in one or many different formats.
- `BarcodeReaderView`: Reads a barcode by using the camera of the device.

### BarcodeReaderView

#### Basic usage

The `BarcodeReaderView` can be used to read a barcode of a format of your choosing.

To use the `BarcodeReaderView` add it to your layout like this:


```xml 
<com.github.wrdlbrnft.betterbarcodes.views.reader.BarcodeReaderView
    android:id="@+id/barcode_reader"
    android:layout_width="match_parent"
    android:layout_height="250dp"
    app:format="qr_code|code128"/>
```

The custom attribute `format` can be used to set the barcode formats you want to read. You can also do that at runtime by using the `setFormat()` method:

```java
barcodeReaderView.setFormat(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128);
```

After that you need to start and stop the reading process by calling `start()` and `stop()`.

 - `start()`: Starts the camera preview and immediately starts looking any barcodes in the cameras field of view.
 - `stop()`: Stops the camera preview and stops looking for barcodes.

Usually you would call `start()` in `onResume()` and `stop()` in `onPause()`. It is not recommended to leave the camera running while your app is in the background.

```java
@Override
public void onResume() {
    super.onResume();
    barcodeReaderView.start();
}

@Override
public void onPause() {
    super.onPause();
    barcodeReaderView.stop();
}
```

You can also independently control preview and scanning by calling `startPreview()` and `startScanning()` as well as `stopScanning()` and `stopPreview()`. However you cannot scan for barcodes without the preview running so calling `startScanning()` will also start the camera preview. In the same way `stopPreview()` will also stop scanning for barcodes.

#### Runtime Permissions

On API levels 23 and above you need handle the runtime permission for the camera which the `BarcodeReaderView` has to use. There are two options:

 - Either you can handle them entirely on your own. In that case you need to make sure that `start()` is only called once the permission has been granted
 - However the preferable option is to use the permission request logic which is built into the `BarcodeReaderView`! 
 
Using the built-in permission request logic is simple:

You have to set a `PermissionHandler` on the `BarcodeReaderView`. The `PermissionHandler` has callback methods to easily handle permission requests, show the permission rational when required and callbacks when the permission is granted or denied. To simplify implementing the `PermissionHandler` there is the `PermissionHandler.Adapter` class. The basic implementation - in this example in a `Fragment` - looks like this:

```java
private PermissionRequest mPermissionRequest;

@Override
public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    barcodeReaderView.setCameraPermissionHandler(new PermissionHandler.Adapter() {
        @Override
        public void onNewPermissionRequest(PermissionRequest request) {
            mPermissionRequest = request;
            request.start(ExampleFragment.this);
        }
    });
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    mPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
}
```

In the `onNewPermissionRequest()` method of the `PermissionHandler` you have to save the `PermissionRequest` instance that is passed in a field. By calling `start()` on the `PermissionRequest` with the current instance of your `Fragment` or `Activity` you start the actual permission request process. Later in the `onRequestPermissionsResult()` callback in your `Fragment` or `Activty` you have to call the method of the same name on the `PermissionRequest` and pass in the parameters. This implementation is enough to handle a complete request for the permission. However you can also override additional methods to show a rationale and to handle cases where the permission is granted or denied:

```java
barcodeReaderView.setCameraPermissionHandler(new PermissionHandler.Adapter() {

    @Override
    public void onNewPermissionRequest(PermissionRequest request) {
        mPermissionRequest = request;
        request.start(ReaderFragment.this);
    }

    @Override
    public boolean onShowRationale() {

        final AlertDialog rationaleDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.rationale_title)
                .setMessage(R.string.rationale_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int button) {
                        dialogInterface.dismiss();
                        
                        // Call this to continue the request process after showing your rationale in whatever way you want.
                        mPermissionRequest.continueAfterRationale(ExampleFragment.this);
                    }
                })
                .create();

        rationaleDialog.show();
        
        // Return true here if you show a rationale
        return true;
    }

    @Override
    public void onPermissionGranted() {
        // Called when the permission is granted
    }

    @Override
    public void onPermissionDenied() {
        // Called when the permission is denied
    }
});
```

### BarcodeView

The `BarcodeView` can be used to display barcodes in one or more formats. How the barcodes are displayed on the screen is controlled by a `BarcodeLayoutManager` instance. BetterBarcodes comes with a few predefined, ready to use `BarcodeLayoutManagers`.

To use the `BarcodeView` add it to your layout like this:

```xml
<com.github.wrdlbrnft.betterbarcodes.views.writer.BarcodeView
    android:id="@+id/barcode_view"
    android:layout_width="match_parent"
    android:layout_height="250dp"
    app:format="qr_code|code128"
    app:text="@string/barcode_text">
```

The custom attribute `format` can be used to set the formats you want to display your barcode in. The custom attribute `text` is used to set the text the barcode should display, this can be a string or a string resource. 

Of course you can also set both of those things at runtime:

```java
barcodeView.setFormat(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128);
barcodeView.setText("Some Text");
```

Per default the `BarcodeView` uses the `HorizontalRotatingLayoutManager`. BetterBarcodes includes the following `BarcodeLayoutManager` implementations:

 - `SimpleVerticalBarcodeLayoutManager`
 - `LinearBarcodeLayoutManager`
 - `HorizontalRotatingLayoutManager`
 
If you want to use one of these you can set it using the `setLayoutManager()` method. Of course you can always implement your own `BarcodeLayoutManager`! To simplify this BetterBarcodes includes the `AbsBarcodeLayoutManager` class which you should use if you want to implement your own one.

### Usage without the Views

If you just want to create a barcode image without using the `BarcodeView` you can do that like this:

```java
final BarcodeWriter writer = BarcodeWriters.forFormat(BarcodeFormat.QR_CODE);
final Bitmap barcodeImage = writer.write("Some Text", someWidth, someHeight);
```

You can also read barcodes without using the `BarcodeReaderView`. 

You can either place an `AspectRatioTextureView` in your layout and then create a `BarcodeReader` instance from it to read barcodes using them camera:

```java
final BarcodeReader reader = BarcodeReaders.get(context, aspectRatioTextureView);
reader.setFormat(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128);

reader.startPreview();
reader.startScanning();
...
reader.stopScanning();
reader.stopPreview();
```

The `BarcodeReader` object works in principle the same way as the `BarcodeReaderView` does - including the way you can handle runtime permissions.

However you can also read barcodes in bitmaps and images from other sources by using a `BarcodeImageDecoder`. It is recommended to use a try/catch/finally block like below to reliably read barcodes:

```java
final BarcodeImageDecoder decoder = BarcodeImageDecoders.forFormat(context, BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128);

try {
    final String text = decoder.decode(someImageAsByteArray, imageWidth, imageHeight);
    // Barcode found and decoded
} catch (FormatException | ChecksumException | NotFoundException e) {
    // No Barcode found in the image or barcode is invalid.
} finally {
    // Every time a barcode is decoded you have to reset the BarcodeImageDecoder
    decoder.reset();
}
```

## Based on ZXing

BetterBarcodes is using ZXing for encoding and decoding barcodes, check out their GitHub page [**here**](https://github.com/zxing/zxing).
