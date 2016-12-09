# BetterBarcodes

The best way to read and display barcodes. 

- **Works on API level 14 and above**: That's 97% of all Android devices. BetterBarcodes takes care backwards compatibility so you don't have to worry about a thing!
- **Extensive support of barcode formats**: Read and display 1D or 2D barcodes in almost all common formats.
- **No lag, no loading times**: Uses the newest API and most efficient implementation to ensure the best possible performance and user experience.

## How I add it to my project?

Just add this to the dependencies closure in your build.gradle:

```groovy
compile 'com.github.wrdlbrnft:better-barcodes:0.2.0.22'
```

## Features

BetterBarcodes includes two ready to use Views which you can add to your layout:

- `BarcodeView`: Displays a barcode in one or many different formats.
- `BarcodeReaderView`: Reads a barcode by using the camera of the device.

### BarcodeReaderView

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

## Usage without the Views

If you just want to create a barcode image without using the `BarcodeView` you can do that like this:

```java
final BarcodeWriter writer = BarcodeWriters.forFormat(BarcodeFormat.QR_CODE);
final Bitmap barcodeImage = writer.write("Some Text", someWidth, someHeight);
```

You can also read barcodes without using the `BarcodeReaderView`, however you need to use the `AspectRatioTextureView` which is also included in the BetterBarcodes library:

```java
final BarcodeReader reader = BarcodeReaders.get(getContext(), aspectRatioTextureView);
reader.setFormat(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128);

reader.startPreview();
reader.startScanning();
...
reader.stopScanning();
reader.stopPreview();
```

The `BarcodeReader` object works in principle the same way as the `BarcodeReaderView` does.

## Based on ZXing

BetterBarcodes is using ZXing for encoding and decoding barcodes, check out their GitHub page [here](https://github.com/zxing/zxing).
