package com.regula.documentreader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.regula.sdk.CaptureActivity;
import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.MRZDetectorErrorCode;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int BROWSE_PICTURE = 2, READER_REQUEST_CODE= 3;
    private ImageButton mCameraBtn, mFolderBtn, mAboutBtn, mSettingBtn;
    private static boolean sIsInitialized;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraBtn = (ImageButton) findViewById(R.id.cameraBtn);
        mFolderBtn = (ImageButton) findViewById(R.id.folderBtn);
        mAboutBtn = (ImageButton) findViewById(R.id.aboutBtn);
        mSettingBtn = (ImageButton) findViewById(R.id.settingBtn);

        mPreferences = getSharedPreferences(StringKeys.REGULA_LIVE_MRZ, MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!sIsInitialized) {
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
                byte[] license =byteArrayOutputStream.toByteArray();
                sIsInitialized = DocumentReader.Instance().Init(MainActivity.this, license);
                licInput.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (sIsInitialized) {
            mCameraBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    int cameraId = mPreferences.getInt(StringKeys.SELECTED_CAMERA_ID,-1);
                    if(cameraId!=-1) {
                        intent.putExtra(DocumentReader.CAMERA_ID,cameraId);
                    }
                    MainActivity.this.startActivityForResult(intent, READER_REQUEST_CODE);
                }
            });

            mAboutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DocumentReader.Instance().about(MainActivity.this);
                }
            });

            mFolderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        } else {
                            startPictureChoosing();
                        }
                    } else {
                        startPictureChoosing();
                    }
                }
            });

            mSettingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(intent);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPictureChoosing();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == BROWSE_PICTURE){
                if (data!=null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Bitmap bmp = getBitmap(selectedImage);
                    int status = DocumentReader.Instance().processBitmap(bmp);
                    if(status == MRZDetectorErrorCode.MRZ_RECOGNIZED_CONFIDENTLY) {
                        Intent i = new Intent(MainActivity.this, ResultsActivity.class);
                        MainActivity.this.startActivity(i);
                    } else{
                        Toast.makeText(MainActivity.this, R.string.no_mrz,Toast.LENGTH_LONG).show();
                    }
                }
            } else if(requestCode == READER_REQUEST_CODE){
                Intent i = new Intent(MainActivity.this, ResultsActivity.class);
                MainActivity.this.startActivity(i);
            }
        }
    }

    private void startPictureChoosing() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= 18) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), BROWSE_PICTURE);
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
