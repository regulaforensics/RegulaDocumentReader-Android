package com.regula.documentreader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.util.HashMap;

public class SettingsActivity extends Activity {
    private static final int imgFolderRequest = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 2;

    private LinearLayout camerasLayout;
    private RadioGroup camerasGroup;
    private TextView horizontalAngleTv,verticalAngleTv;
    private Switch imgSavingSwitch;
    private SharedPreferences prefs;
    private Button imgSavingPathBtn;
    private EditText imgSavingPath;
    private HashMap<Integer,String> camerasHorAngle, camerasVerAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(StringKeys.REGULA_LIVE_MRZ, MODE_PRIVATE);

        camerasLayout = (LinearLayout) findViewById(R.id.camerasLayout);
        camerasGroup = (RadioGroup) findViewById(R.id.camerasRG);
        horizontalAngleTv = (TextView) findViewById(R.id.horizontalAngleTv);
        verticalAngleTv = (TextView) findViewById(R.id.verticalAngleTv);

        imgSavingSwitch = (Switch) findViewById(R.id.imgSavingSwitch);
        imgSavingPathBtn = (Button) findViewById(R.id.imgSavingBrowseBtn);
        imgSavingPath = (EditText) findViewById(R.id.imgSavingPath);

        camerasHorAngle = new HashMap<>();
        camerasVerAngle = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SettingsActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

        } else { //Permission is granted
            FillCameras();
        }

        boolean doSaveImages = prefs.getBoolean(StringKeys.SAVE_IMAGES, false);
        imgSavingSwitch.setChecked(doSaveImages);
        imgSavingPathBtn.setEnabled(doSaveImages);

        String imagePath = prefs.getString(StringKeys.IMG_SAVING_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "/RegulaDocumentReader/");
        imgSavingPath.setText(imagePath);

        imgSavingPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent chooserIntent = new Intent(SettingsActivity.this, DirectoryChooserActivity.class);

                final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                        .newDirectoryName("new")
                        .allowReadOnlyDirectory(false)
                        .allowNewDirectoryNameModification(true)
                        .initialDirectory("/sdcard")
                        .build();

                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
                startActivityForResult(chooserIntent, imgFolderRequest);
            }
        });

        if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            imgSavingSwitch.setChecked(false);
            imgSavingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    ActivityCompat.requestPermissions(SettingsActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                }
            });

        } else {
            imgSavingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DoSaveImages(isChecked);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case imgFolderRequest:
                if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                    String imagePath = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                    prefs.edit().putString(StringKeys.IMG_SAVING_PATH, imagePath).commit();
                } // else Nothing selected
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imgSavingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            DoSaveImages(isChecked);
                        }
                    });

                    DoSaveImages(true);
                }
            }
            case MY_PERMISSIONS_REQUEST_CAMERA:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FillCameras();
                }
            }
        }
    }

    private void DoSaveImages(boolean isChecked) {
        imgSavingPathBtn.setEnabled(isChecked);
        prefs.edit().putBoolean(StringKeys.SAVE_IMAGES, isChecked).apply();
    }

    private void FillCameras() {
        camerasLayout.setVisibility(View.VISIBLE);
        camerasGroup.removeAllViews();
        int selectedCamera = prefs.getInt(StringKeys.SELECTED_CAMERA_ID, -1);
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Camera camera = Camera.open(i);
                Camera.Parameters parameters = camera.getParameters();
                camera.release();

                String horAngle = String.valueOf(parameters.getHorizontalViewAngle());
                String verAngle = String.valueOf(parameters.getVerticalViewAngle());

                RadioButton rdbtn = new RadioButton(this);
                rdbtn.setId(i);
                rdbtn.setText(getString(R.string.camera) + " " + rdbtn.getId());
                rdbtn.setOnClickListener(rbListener);

                if (i == selectedCamera) {
                    rdbtn.setChecked(true);

                    horizontalAngleTv.setText(String.format(getString(R.string.camera_hor_angle), horAngle));
                    verticalAngleTv.setText(String.format(getString(R.string.camera_ver_angle), verAngle));
                }

                camerasGroup.addView(rdbtn);

                camerasVerAngle.put(i, verAngle);
                camerasHorAngle.put(i, horAngle);
            }
        }

        if(camerasGroup.getChildCount()==1 || selectedCamera==-1){
            RadioButton button = (RadioButton) camerasGroup.getChildAt(0);
            button.performClick();
        }
    }

    View.OnClickListener rbListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            horizontalAngleTv.setText(String.format(getString(R.string.camera_hor_angle), camerasHorAngle.get(v.getId())));
            verticalAngleTv.setText(String.format(getString(R.string.camera_ver_angle), camerasVerAngle.get(v.getId())));

            prefs.edit().putInt(StringKeys.SELECTED_CAMERA_ID, v.getId()).apply();
        }
    };
}


