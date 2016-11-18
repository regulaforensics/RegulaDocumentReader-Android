package com.regula.documentreader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.regula.sdk.results.TextField;
import com.regula.sdk.translation.TranslationUtil;

import java.util.List;

public class SimpleMrzDataAdapter extends ArrayAdapter<TextField> {

	public SimpleMrzDataAdapter(Context context, int resource, List<TextField> objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		if (v == null) {

			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.simple_data_layout, null);
		}

        TextField p = getItem(position);

		if (p != null) {

			TextView name = (TextView) v.findViewById(R.id.nameTv);
			TextView textValue = (TextView) v.findViewById(R.id.valueTv);
			LinearLayout layout = (LinearLayout) v.findViewById(R.id.simpleItemLayout);

			textValue.setTypeface(Typeface.MONOSPACE);

			name.setText(TranslationUtil.getTextFieldTranslation(getContext(),p.fieldType));

			if (p.bufText != null) {
				String textValueText = p.bufText.replace("^", "\n");
				textValue.setText(textValueText);
			}

			if (p.validity == 1)
				textValue.setTextColor(Color.rgb(3, 140, 7));
			else if (p.validity == 0)
				textValue.setTextColor(Color.BLACK);
			else {
				if (p.reserved2 != -1)
					textValue.setText(textValue.getText() + " (" + p.reserved2 + ")");
				textValue.setTextColor(Color.RED);
			}

			layout.setBackgroundColor(position % 2 > 0 ? Color.rgb(228, 228, 237) : Color.rgb(237, 237, 228));
		}

		return v;
	}
}
