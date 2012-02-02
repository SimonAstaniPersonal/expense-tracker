/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/     


package com.vinsol.expensetracker.listing;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.vinsol.expensetracker.Constants;
import com.vinsol.expensetracker.R;
import com.vinsol.expensetracker.helpers.DisplayDate;
import com.vinsol.expensetracker.models.Entry;

public class ExpenseSubListing extends ListingAbstract {

	private TextView listingHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		intentExtras.putBoolean(Constants.IS_COMING_FROM_EXPENSE_LISTING, true);
		initListView();
	}
	
	private void initListView() {
		mSeparatedListAdapter = new SeparatedListAdapter(this,highlightID);
		intentExtras = getIntent().getExtras();
		listingHeader = (TextView) findViewById(R.id.expense_listing_header_title);
		Entry entry = intentExtras.getParcelable(Constants.ENTRY_LIST_EXTRA);
		mDataDateList = mConvertCursorToListString.getDateListString(false,entry.id,type);
		mSubList = mConvertCursorToListString.getListStringParticularDate(entry.id);
		if(mSubList.size() > 0) {
			Calendar mTempCalendar = Calendar.getInstance();
			mTempCalendar.setTimeInMillis(mSubList.get(0).timeInMillis);
			mTempCalendar.set(mTempCalendar.get(Calendar.YEAR),mTempCalendar.get(Calendar.MONTH),mTempCalendar.get(Calendar.DAY_OF_MONTH),0,0,0);
			mTempCalendar.setFirstDayOfWeek(Calendar.MONDAY);
			listingHeader.setText(new DisplayDate(mTempCalendar).getHeaderFooterListDisplayDate(getSubListHeaderType()));
			addSections();
		} else {
			Intent mIntent = new Intent(this, ExpenseListing.class);
			mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mIntent);
			finish();
		}
	}
	
	private int getSubListHeaderType() {
		switch (type) {
		case R.string.sublist_thisyear:
			return R.string.sublist_all;
		case R.string.sublist_thismonth:
			return R.string.sublist_thismonth;
		case R.string.sublist_thisweek:
			return 0;
		default:
			return R.string.sublist_thisweek;
		}
	}
	
	@Override
	protected void unknownDialogAction(String id) {
		initListView();
	}
	
	@Override
	protected void noItemButtonAction(Button noItemButton) {
		noItemButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, ExpenseListing.class);
		intent.putExtras(intentExtras);
		setResult(Activity.RESULT_OK, intent);
		super.onBackPressed();
	}
	
	@Override
	public void noItemLayout() {
		if(mSeparatedListAdapter.getDataDateList() == null || mSeparatedListAdapter.getDataDateList().isEmpty()) {
			finish();
		}
	}
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.expense_listing_no_tab);
	}

	@Override
	protected boolean condition(DisplayDate mDisplayDate) {
		return true;
	}
	
	@Override
	protected int getType(Bundle intentExtras) {
		return intentExtras.getInt(Constants.TYPE);
	}
	
}
