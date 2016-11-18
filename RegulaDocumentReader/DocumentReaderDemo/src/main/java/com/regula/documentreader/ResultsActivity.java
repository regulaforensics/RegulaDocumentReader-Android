package com.regula.documentreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.eGraphicFieldType;
import com.regula.sdk.results.GraphicField;
import com.regula.sdk.results.TextField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ResultsActivity extends Activity {

    private ImageView mrzImgView;
    private SimpleMrzDataAdapter mAdapter;
    private List<TextField> mResultItems;
    private SharedPreferences mPreferences;
    private boolean isFirstStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results);
        mrzImgView = (ImageView) findViewById(R.id.mrzImgView);
        ListView mrzItemsList = (ListView) findViewById(R.id.mrzItemsList);
        mResultItems = new ArrayList<>();

        mAdapter = new SimpleMrzDataAdapter(ResultsActivity.this,0,mResultItems);
        mrzItemsList.setAdapter(mAdapter);

        mPreferences = getSharedPreferences(StringKeys.REGULA_LIVE_MRZ, Context.MODE_PRIVATE);

        isFirstStart = savedInstanceState==null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPreferences.getBoolean(StringKeys.SAVE_IMAGES, false) && isFirstStart) {
            Bitmap sourceImg = DocumentReader.Instance().getSourceImage();

            if (sourceImg != null) {
                OutputStream outStream = null;
                Calendar calendar = Calendar.getInstance();

                String imgPath = mPreferences.getString(StringKeys.IMG_SAVING_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/RegulaDocumentReader/");
                File dir = new File(imgPath + calendar.get(Calendar.YEAR) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE) + "/");
                try {
                    dir.mkdirs();
                    File file = new File(dir, System.currentTimeMillis() + ".jpg");
                    outStream = new FileOutputStream(file);
                    sourceImg.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outStream != null) {
                            outStream.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        mResultItems.addAll(DocumentReader.Instance().getAllTextFields());
        mAdapter.notifyDataSetChanged();

        GraphicField graphicField = DocumentReader.Instance().getGraphicFieldByType(eGraphicFieldType.gt_Other);
        if (graphicField != null && graphicField.fileImage != null) {
            mrzImgView.setImageBitmap(graphicField.fileImage);
        } else {
            mrzImgView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }
}
