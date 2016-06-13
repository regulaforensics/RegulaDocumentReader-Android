package com.regula.documentreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.regula.sdk.CaptureActivity;
import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.MRZDetectorErrorCode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    private ImageButton mCameraBtn, mFolderBtn, mAboutBtn;
    private DocumentReader mDocumentReader;
    private static boolean sIsLicenseOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraBtn = (ImageButton) findViewById(R.id.cameraBtn);
        mFolderBtn = (ImageButton) findViewById(R.id.folderBtn);
        mAboutBtn = (ImageButton) findViewById(R.id.aboutBtn);

        mDocumentReader = DocumentReader.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!sIsLicenseOk) {
            try {
                InputStream licInput = getResources().openRawResource(R.raw.regula);
                byte[] license = new byte[licInput.available()];
                licInput.read(license);
                sIsLicenseOk = mDocumentReader.setLibLicense(license);
                licInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (sIsLicenseOk) {
            mCameraBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    MainActivity.this.startActivityForResult(intent, DocumentReader.READER_REQUEST_CODE);
                }
            });

            mAboutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DocumentReader.getInstance().about();
                }
            });

            mFolderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                }
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.strError);
            builder.setMessage(R.string.strLicenseInvalid);
            builder.setPositiveButton(R.string.strOK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    System.exit(0);
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1){
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Bitmap bmp = getBitmap(selectedImage);
                    int status = mDocumentReader.processBitmap(bmp);
                    if(status == MRZDetectorErrorCode.MRZ_RECOGNIZED_CONFIDENTLY) {
                        Intent i = new Intent(MainActivity.this, ResultsActivity.class);
                        MainActivity.this.startActivity(i);
                    } else{
                        Toast.makeText(MainActivity.this, R.string.no_mrz,Toast.LENGTH_LONG).show();
                    }
                }
            } else if(requestCode == DocumentReader.READER_REQUEST_CODE){
                Intent i = new Intent(MainActivity.this, ResultsActivity.class);
                MainActivity.this.startActivity(i);
            }
        }
    }

    private Bitmap getBitmap(Uri selectedImage) {
        ContentResolver resolver = MainActivity.this.getContentResolver();
        InputStream is = null;
        try {
            is = resolver.openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);

        //Re-reading the input stream to move it's pointer to start
        try {
            is = resolver.openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 1280, 720);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
