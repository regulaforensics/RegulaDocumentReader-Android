# Regula Document Reader
If you have any questions, feel free to contact us at support@regulaforensics.com

<img src="DocumentReaderDemo.png" width="500">

* [How to build demo application](#how_to_build_demo_application)
* [How to use DocumentReader library](#how_to_use_documentreader_library)
* [How to add DocumentReader library to your project](#how_to_add_documentreader_library_to_your_project)
* [Additional information](#additional_information)

## <a name="how_to_build_demo_application"></a> How to build demo application
1. Get trial license for demo application at [licensing.regulaforensics.com](https://licensing.regulaforensics.com) (`regula.license` file).
1. Clone current repository using command `git clone https://github.com/regulaforensics/RegulaDocumentReader-Android.git`.
1. Download and install latest [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
1. Download and install latest [Android Studio](https://developer.android.com/studio/index.html).
1. Download latest [DocumentReader.aar](https://github.com/regulaforensics/RegulaDocumentReader-Android/releases/latest) and copy it to `RegulaDocumentReader/DocumentReader` folder.
1. Copy file `regula.license` to `RegulaDocumentReader/DocumentReaderDemo/src/main/res/raw` folder. 
1. Launch Android Studio and select _Open an existing Android Studio project_ then select _RegulaDocumentReader_ project in file browser.
1. Download additional files proposed by Android Studio to build project (build tools, for example).
1. Build and run application on device.

## <a name="how_to_use_documentreader_library"></a> How to use DocumentReader library
The very first step you should make is install license file:
```java
boolean licenseOk = false;
try {
    InputStream licInput = getResources().openRawResource(R.raw.regula);
    byte[] license = new byte[licInput.available()]; // license - it is an array of bytes
    licInput.read(license);
    licenseOk = DocumentReader.setLibLicense(license);
    licInput.close();
} catch (IOException e) {
    e.printStackTrace();
}
if (licenseOk) {
    // license is OK, now we can use DocumentReader library
}
else {
    // license isn't OK, you can see additional information in logcat
}
```

License file contains information about your application id and time terms. If `setLibLicense()` method returns false, you can see additional information in logcat.

When the license file is installed, all you need to do is to call only one function to process bitmap or video frame:
```java
// Bitmap processing
Bitmap bmp = getBitmap(selectedImage);
int status = DocumentReader.processBitmap(bmp);
if(status == MRZDetectorErrorCode.MRZ_RECOGNIZED_CONFIDENTLY) {
  // MRZ recognized, fetch results
  TextField surnameTextField = DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname);
  String surname = surnameTextField.bufText;
  ...
} else {
  // MRZ not recognized
}

// Video frame processing (Camera.PreviewCallback interface, android.hardware.camera2 API)
private CameraPreview camPreview;
...
@override
public void onPreviewFrame(byte[] data, final Camera camera) {
    int status = DocumentReader.processVideoFrame(
      data, camPreview.camW, camPreview.camH, camPreview.params.getPreviewFormat());
    if (status == MRZDetectorErrorCode.MRZ_RECOGNIZED_CONFIDENTLY) {
      // MRZ recognized, fetch results
      TextField surnameTextField = DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname);
      String surname = surnameTextField.bufText;   
      ...
    }
    else {
      // MRZ not recognized
    }
}
```

You can also use `CaptureActivity` that does all camera work for you:
```java
Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
MainActivity.this.startActivityForResult(intent, DocumentReader.READER_REQUEST_CODE);
...
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == DocumentReader.READER_REQUEST_CODE){
        // MRZ recognized, fetch results
        TextField surnameTextField = DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname);
        String surname = surnameTextField.bufText;
        ...
    }
}
```

Additional details of how to use `CaptureActivity` you can find in demo application code.

## <a name="how_to_add_documentreader_library_to_your_project"></a> How to add DocumentReader library to your project
1. Open your project in Android Studio.
1. In _File_ menu select _New_ submenu and thant select _New Module..._.
1. In appeared window select _Import .JAR/.AAR Package_.
1. In field _File name:_ write path to `DocumentReader.aar` file which you can find in `RegulaDocumentReader/DocumentReader` folder and press _Finish_ button.
1. In your project `build.gradle` file add dependency to DocumentReader library:
```
dependencies {
    compile project(':DocumentReader')
}
```
You also have to register license file as described in [How to use DocumentReader library](#how_to_use_documentreader_library)

## <a name="additional_information"></a> Additional information
[Javadoc API reference](https://regulaforensics.github.io/RegulaDocumentReader-Android/index.html). 

If you have any questions, feel free to contact us at support@regulaforensics.com
