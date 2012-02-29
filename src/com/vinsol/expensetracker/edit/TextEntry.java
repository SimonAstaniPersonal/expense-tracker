/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/     


package com.vinsol.expensetracker.edit;

import android.os.Bundle;
import android.widget.TextView;

import com.vinsol.expensetracker.R;

public class TextEntry extends EditAbstract {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		typeOfEntry = R.string.text;
		typeOfEntryFinished = R.string.finished_textentry;
		typeOfEntryUnfinished = R.string.unfinished_textentry;
		editHelper();
	}
	

	@Override
	protected void setDefaultTitle() {
		if(isFromFavorite) {
			((TextView)findViewById(R.id.header_title)).setText(getString(R.string.edit_favorite)+" "+getString(R.string.finished_textentry));
		} else {
			((TextView)findViewById(R.id.header_title)).setText(getString(R.string.finished_textentry));
		}
	}
}
