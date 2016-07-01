package com.regula.documentreader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.eGraphicFieldType;
import com.regula.sdk.results.GraphicField;
import com.regula.sdk.results.TextField;

import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends Activity {

	private ImageView mrzImgView;
	private ListView mrzItemsList;
	private SimpleMrzDataAdapter mAdapter;
    private List<TextField> mResultItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_results);
		mrzImgView = (ImageView) findViewById(R.id.mrzImgView);
		mrzItemsList = (ListView) findViewById(R.id.mrzItemsList);
        mResultItems = new ArrayList<>();

        mAdapter = new SimpleMrzDataAdapter(ResultsActivity.this,0,mResultItems);
        mrzItemsList.setAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();

        mResultItems.addAll(DocumentReader.getAllTextFields());
        mAdapter.notifyDataSetChanged();

        GraphicField graphicField = DocumentReader.getGraphicFieldByType(eGraphicFieldType.gt_Other);
        if(graphicField!=null && graphicField.fileImage!=null) {
            mrzImgView.setImageBitmap(graphicField.fileImage);
        } else {
            mrzImgView.setVisibility(View.GONE);
        }
	}
}
