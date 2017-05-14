Regula Document Reader
========

If you have any questions, feel free to contact us at support@regulaforensics.com

.. image:: DocumentReaderDemo.png

* `How to build demo application`_
* `How to use DocumentReader library`_
* `How to add DocumentReader library to your project`_
* `Troubleshooting license issues`_
* `Additional information`_

.. _`How to build demo application`:
How to build demo application
----
1. Get trial license for demo application at `licensing.regulaforensics.com <https://licensing.regulaforensics.com>`__ (``regula.license`` file).
2. Clone current repository using command ``git clone https://github.com/regulaforensics/RegulaDocumentReader-Android.git``.
3. Download and install latest `JDK <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`__.
4. Download and install latest `Android Studio <https://developer.android.com/studio/index.html>`__.
5. Download latest `DocumentReader.aar <https://github.com/regulaforensics/RegulaDocumentReader-Android/releases/latest>`__ and copy it to `RegulaDocumentReader/DocumentReader` folder.
6. Copy file ``regula.license`` to ``RegulaDocumentReader/DocumentReaderDemo/src/main/res/raw`` folder. 
7. Launch Android Studio and select ``Open an existing Android Studio project`` then select ``RegulaDocumentReader`` project in file browser.
8. Download additional files proposed by Android Studio to build project (build tools, for example).
9. Build and run application on device.

.. _`How to use DocumentReader library`:
How to use DocumentReader library
----
The very first step you should make is initialize DocumentReader (install license file):

.. code-block:: java

  try {
      InputStream licInput = getResources().openRawResource(R.raw.regula);
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      int i;
      try {
          i = licInput.read();
          while (i != -1)
          {
              byteArrayOutputStream.write(i);
              i = licInput.read();
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
      byte[] license = byteArrayOutputStream.toByteArray();
      sIsInitialized = DocumentReader.Instance().Init(MainActivity.this, license);
      licInput.close();
      byteArrayOutputStream.close();
  } catch (IOException e) {
      e.printStackTrace();
  }

License file contains information about your application id and time terms. If ``Init()`` method returns false, you can see additional information in logcat.

When DocumentReader is initialized, all you need to do is to call only one function to process bitmap or video frame:

.. code-block:: java

  // Bitmap processing
  Bitmap bmp = getBitmap(selectedImage);
  int status = DocumentReader.Instance().processBitmap(bmp);
  if(status == MRZDetectorErrorCode.MRZ_RECOGNIZED_CONFIDENTLY) {
      // MRZ recognized, fetch results
      TextField surnameTextField = DocumentReader.Instance().getTextFieldByType(eVisualFieldType.ft_Surname);
      String surname = surnameTextField.bufText;
      ...
  } else{
      // MRZ not recognized
      ...
  }
  
  // Video frame processing (Camera.PreviewCallback interface, android.hardware.camera2 API)
  private CameraPreview camPreview;
  ...
  @override
  public void onPreviewFrame(byte[] data, final Camera camera) {
      ...
      int status = DocumentReader.Instance().processVideoFrame(data, size.width, size.height, parameters.getPreviewFormat());
      if (status == MRZDetectorErrorCode.MRZ_RECOGNIZED_CONFIDENTLY) {
          // MRZ recognized, fetch results
          TextField surnameTextField = DocumentReader.Instance().getTextFieldByType(eVisualFieldType.ft_Surname);
          String surname = surnameTextField.bufText; 
            ...
      }
      else {
          // MRZ not recognized
          ...
      }
  }

You can also use ``CaptureActivity`` that does all camera work for you:

.. code-block:: java

  Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
  MainActivity.this.startActivityForResult(intent, DocumentReader.READER_REQUEST_CODE);
  ...
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (resultCode == RESULT_OK && requestCode == DocumentReader.READER_REQUEST_CODE){
          // MRZ recognized, fetch results
          TextField surnameTextField = DocumentReader.Instance().getTextFieldByType(eVisualFieldType.ft_Surname);
          String surname = surnameTextField.bufText;
          ...
      }
  }

Additional details of how to use ``CaptureActivity`` you can find in demo application code.

.. _`How to add DocumentReader library to your project`:
How to add DocumentReader library to your project
----
1. Open your project in Android Studio.
2. In ``File`` menu select ``New`` submenu and thant select ``New Module...``.
3. In appeared window select ``Import .JAR/.AAR Package``.
4. In field ``File name:`` write path to ``DocumentReader.aar`` file which you can find in ``RegulaDocumentReader/DocumentReader`` folder and press ``Finish`` button.
5. In your project ``build.gradle`` file add dependency to DocumentReader library:

.. code-block:: java

  dependencies {
      compile project(':DocumentReader')
  }

You also have to register license file as described in `How to use DocumentReader library`_

.. _`Troubleshooting license issues`:
Troubleshooting license issues
----
If you have issues with license verification when running the application, please verify that next is true:

1. OS you are using is the same as in the license you generated (Android).
2. Application ID is the same that you specified for license.
3. Date and time on the device you are trying to run the application is correct and inside the license validity term.
4. You are using the latest release of the SDK from `Releases <https://github.com/regulaforensics/RegulaDocumentReader-Android/releases>`__.
5. You placed the license into the correct folder as described here `How to build demo application`_ (``RegulaDocumentReader/DocumentReaderDemo/src/main/res/raw``).

.. _`Additional information`:
Additional information
----
`Javadoc API reference <https://regulaforensics.github.io/RegulaDocumentReader-Android/index.html>`__. 

If you have any questions, feel free to contact us at support@regulaforensics.com
