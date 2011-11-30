package com.vinsol.expensetracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.vinsol.expensetracker.location.LocationLast;
import com.vinsol.expensetracker.utils.AudioPlay;
import com.vinsol.expensetracker.utils.DateHelper;
import com.vinsol.expensetracker.utils.DisplayTime;
import com.vinsol.expensetracker.utils.FileDelete;
import com.vinsol.expensetracker.utils.RecordingHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Voice extends Activity implements OnClickListener {

	private TextView text_voice_camera_header_title;
	private ImageView text_voice_camera_voice_details_separator;
	private RelativeLayout text_voice_camera_voice_details;
	private Chronometer text_voice_camera_time_details_chronometer;
	private Button text_voice_camera_stop_button;
	private Button text_voice_camera_play_button;
	private Button text_voice_camera_rerecord_button;
	private EditText text_voice_camera_amount;
	private EditText text_voice_camera_tag;
	private MyCount countDownTimer;
	private RecordingHelper mRecordingHelper;
	private AudioPlay mAudioPlay;
	private long _id;
	private Bundle intentExtras;
	private DatabaseAdapter mDatabaseAdapter;
	private TextView text_voice_camera_date_bar_dateview;
	private String dateViewString;
	private ArrayList<String> mEditList;
	private boolean setLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ///// ****** No Title Bar ********* /////////

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.text_voice_camera);

		// ////// ******** Initializing and assigning memory to UI Items
		// ********** /////////

		text_voice_camera_header_title = (TextView) findViewById(R.id.text_voice_camera_header_title);
		text_voice_camera_voice_details_separator = (ImageView) findViewById(R.id.text_voice_camera_voice_details_separator);
		text_voice_camera_voice_details = (RelativeLayout) findViewById(R.id.text_voice_camera_voice_details);
		text_voice_camera_time_details_chronometer = (Chronometer) findViewById(R.id.text_voice_camera_time_details_chronometer);
		text_voice_camera_stop_button = (Button) findViewById(R.id.text_voice_camera_stop_button);
		text_voice_camera_play_button = (Button) findViewById(R.id.text_voice_camera_play_button);
		text_voice_camera_rerecord_button = (Button) findViewById(R.id.text_voice_camera_rerecord_button);
		text_voice_camera_amount = (EditText) findViewById(R.id.text_voice_camera_amount);
		text_voice_camera_tag = (EditText) findViewById(R.id.text_voice_camera_tag);
		text_voice_camera_date_bar_dateview = (TextView) findViewById(R.id.text_voice_camera_date_bar_dateview);
		mDatabaseAdapter = new DatabaseAdapter(this);

		// //////********* Get id from intent extras ******** ////////////
		intentExtras = getIntent().getBundleExtra("voiceBundle");
		if(intentExtras.containsKey("_id")){
			_id = intentExtras.getLong("_id");
		}

		if(intentExtras.containsKey("setLocation")){
			setLocation = intentExtras.getBoolean("setLocation");
		}
		
		if (intentExtras.containsKey("mDisplayList")) {
			mEditList = new ArrayList<String>();
			mEditList = intentExtras.getStringArrayList("mDisplayList");
			_id = Long.parseLong(mEditList.get(0));
			String amount = mEditList.get(2);
			String tag = mEditList.get(1);
			if (!(amount.equals("") || amount == null)) {
				if (!amount.contains("?"))
					text_voice_camera_amount.setText(amount);
			}
			if (!(tag.equals("") || tag == null || tag
					.equals(getString(R.string.unfinished_voiceentry)))) {
				text_voice_camera_tag.setText(tag);
			}
		}

		// ////// ******** Handle Date Bar ********* ////////
		if (intentExtras.containsKey("mDisplayList")) {
			new DateHandler(this, Long.parseLong(mEditList.get(6)));
		} else if (intentExtras.containsKey("timeInMillis")) {
			new DateHandler(this, intentExtras.getLong("timeInMillis"));
		} else {
			new DateHandler(this);
		}

		// ////// ******** Starts Recording each time activity starts ******
		// ///////
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			setGraphicsVoice();

			if (intentExtras.containsKey("mDisplayList")) {
				File tempFile = new File("/sdcard/ExpenseTracker/Audio/" + _id
						+ ".amr");

				if (tempFile.canRead()) {
					mAudioPlay = new AudioPlay(Long.toString(_id), this);
					text_voice_camera_stop_button.setVisibility(View.GONE);
					text_voice_camera_play_button.setVisibility(View.VISIBLE);
					text_voice_camera_rerecord_button
							.setVisibility(View.VISIBLE);
					text_voice_camera_time_details_chronometer
							.setText(new DisplayTime()
									.getDisplayTime(mAudioPlay
											.getPlayBackTime()));
				} else {
					text_voice_camera_time_details_chronometer
							.setText("Audio File Missing");
					text_voice_camera_rerecord_button
							.setVisibility(View.VISIBLE);
					text_voice_camera_stop_button.setVisibility(View.GONE);
					text_voice_camera_play_button.setVisibility(View.GONE);
				}
			} else {
				mRecordingHelper = new RecordingHelper(_id + "", this);
				mRecordingHelper.startRecording();
				controlVoiceChronometer();
			}
		} else {
			Toast.makeText(this, "sdcard not available", Toast.LENGTH_LONG)
					.show();
		}
		setClickListeners();

		// //////********* Get Last most accurate location info *********
		// /////////
		LocationLast mLocationLast = new LocationLast(this);
		mLocationLast.getLastLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		dateViewString = text_voice_camera_date_bar_dateview.getText()
				.toString();
	}

	@Override
	protected void onPause() {

		// //// ***** Check whether audio is recording or not ******* ///////
		// //// ****** If audio recording started then stop recording audio
		// ***** ///////
		try {
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				if (mRecordingHelper.isRecording()) {
					mRecordingHelper.stopRecording();
				}
			}
			if (mAudioPlay.isAudioPlaying())
				mAudioPlay.stopPlayBack();
		} catch (Exception e) {

		}
		super.onPause();
	}

	private void setClickListeners() {
		// ////// ******* Adding Click Listeners to UI Items ******** //////////

		text_voice_camera_stop_button.setOnClickListener(this);
		text_voice_camera_play_button.setOnClickListener(this);
		text_voice_camera_rerecord_button.setOnClickListener(this);

		Button text_voice_camera_save_entry = (Button) findViewById(R.id.text_voice_camera_save_entry);
		text_voice_camera_save_entry.setOnClickListener(this);

		Button text_voice_camera_delete = (Button) findViewById(R.id.text_voice_camera_delete);
		text_voice_camera_delete.setOnClickListener(this);
	}

	private void controlVoiceChronometer() {
		text_voice_camera_time_details_chronometer.start();
		text_voice_camera_time_details_chronometer
				.setOnChronometerTickListener(new OnChronometerTickListener() {

					@Override
					public void onChronometerTick(Chronometer chronometer) {
						if (text_voice_camera_time_details_chronometer
								.getText().length() > 5) {
							text_voice_camera_time_details_chronometer.stop();
							text_voice_camera_stop_button
									.setVisibility(View.GONE);
							text_voice_camera_play_button
									.setVisibility(View.VISIBLE);
							text_voice_camera_rerecord_button
									.setVisibility(View.VISIBLE);
						}
					}
				});
	}

	private void setGraphicsVoice() {
		// ///// ***** Sets Title Voice Entry *********///////
		text_voice_camera_header_title.setText("Voice Entry");

		// ///// ***** Sets Title Voice Entry *********///////
		text_voice_camera_voice_details_separator.setVisibility(View.VISIBLE);

		// //// ****** Shows Voice Details ********////////
		text_voice_camera_voice_details.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		// ///// ******** Adding On Click Actions to click listeners *********
		// //////////

		// // ***** if stop button pressed ****** //////
		if (v.getId() == R.id.text_voice_camera_stop_button) {
			try {
				countDownTimer.cancel();
			} catch (NullPointerException e) {
			}
			;

			// //// ****** Handles UI items on button click ****** ///////
			text_voice_camera_stop_button.setVisibility(View.GONE);
			text_voice_camera_play_button.setVisibility(View.VISIBLE);
			text_voice_camera_rerecord_button.setVisibility(View.VISIBLE);

			// //// ******* Stop Recording Audio and stop chronometer ********
			// ////////
			try {
				if (mRecordingHelper.isRecording())
					mRecordingHelper.stopRecording();
			} catch (Exception e) {
			}
			text_voice_camera_time_details_chronometer.stop();
			try {
				if (mAudioPlay.isAudioPlaying())
					mAudioPlay.stopPlayBack();
			} catch (Exception e) {
			}
			try {
				text_voice_camera_time_details_chronometer
						.setText(new DisplayTime().getDisplayTime(mAudioPlay
								.getPlayBackTime()));
			} catch (NullPointerException e) {

			}
		}

		// // ***** if play button pressed ****** //////
		else if (v.getId() == R.id.text_voice_camera_play_button) {
			// //// ******** to handle playback of recorded file *********
			// ////////
			mAudioPlay = new AudioPlay(_id + "", this);

			// ///// ******* Chronometer Starts Countdown ****** ///////
			countDownTimer = new MyCount(mAudioPlay.getPlayBackTime(), 1000);

			// //// ****** Handles UI items on button click ****** ///////
			text_voice_camera_play_button.setVisibility(View.GONE);
			text_voice_camera_stop_button.setVisibility(View.VISIBLE);
			text_voice_camera_rerecord_button.setVisibility(View.VISIBLE);

			// /// ******** Start Audio Playback and counter to play audio
			// ****** ///////
			if (!mAudioPlay.isAudioPlaying()) {
				mAudioPlay.startPlayBack();
			} else {
				mAudioPlay.stopPlayBack();
				mAudioPlay.startPlayBack();
			}
			countDownTimer.start();
		}

		// // ***** if rerecord button pressed ****** //////
		else if (v.getId() == R.id.text_voice_camera_rerecord_button) {
			try {
				countDownTimer.cancel();
			} catch (NullPointerException e) {
			}
			;

			// /// ******* If Audio PlayBack is there stop playing audio
			// *******//////
			try {
				if (mAudioPlay.isAudioPlaying()) {
					mAudioPlay.stopPlayBack();
				}
			} catch (NullPointerException e) {
			}

			// //// ****** Handles UI items on button click ****** ///////
			text_voice_camera_play_button.setVisibility(View.GONE);
			text_voice_camera_stop_button.setVisibility(View.VISIBLE);
			text_voice_camera_rerecord_button.setVisibility(View.GONE);

			// //// ****** Restarts chronometer and recording ******* ////////
			if (mRecordingHelper.isRecording())
				mRecordingHelper.stopRecording();
			mRecordingHelper = new RecordingHelper(_id + "", this);
			mRecordingHelper.startRecording();
			text_voice_camera_time_details_chronometer.setBase(SystemClock
					.elapsedRealtime());
			text_voice_camera_time_details_chronometer.start();
		}

		// //////******** Adding Action to save entry ********* ///////////

		if (v.getId() == R.id.text_voice_camera_save_entry) {
			saveEntry();
		}

		// /////// ********* Adding action if delete button ********** /////////

		if (v.getId() == R.id.text_voice_camera_delete) {
			// //// ***** Check whether audio is recording or not *******
			// ///////
			// //// ****** If audio recording started then stop recording audio
			// ***** ///////
			try {
				if (mRecordingHelper.isRecording()) {
					mRecordingHelper.stopRecording();
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			// /// ******* If Audio PlayBack is there stop playing audio
			// *******//////
			try {
				if (mAudioPlay.isAudioPlaying()) {
					mAudioPlay.stopPlayBack();
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			text_voice_camera_time_details_chronometer.stop();

			new FileDelete(_id);

			// //// ******* Delete entry from database ******** /////////
			mDatabaseAdapter.open();
			mDatabaseAdapter.deleteDatabaseEntryID(Long.toString(_id));
			mDatabaseAdapter.close();
			finish();
		}
	}

	private void saveEntry() {
		// ///// ******* Creating HashMap to update info ******* ////////
		HashMap<String, String> _list = new HashMap<String, String>();
		_list.put(DatabaseAdapter.KEY_ID, Long.toString(_id));

		if (!text_voice_camera_amount.getText().toString().equals(".")
				&& !text_voice_camera_amount.getText().toString().equals("")) {
			Double mAmount = Double.parseDouble(text_voice_camera_amount
					.getText().toString());
			mAmount = (double) ((int) ((mAmount + 0.005) * 100.0) / 100.0);
			_list.put(DatabaseAdapter.KEY_AMOUNT, mAmount.toString());
		} else {
			_list.put(DatabaseAdapter.KEY_AMOUNT, null);
		}
		if (text_voice_camera_tag.getText().toString() != "") {
			_list.put(DatabaseAdapter.KEY_TAG, text_voice_camera_tag.getText()
					.toString());
		}

		if (!text_voice_camera_date_bar_dateview.getText().toString()
				.equals(dateViewString)) {
			try {
				if (!intentExtras.containsKey("mDisplayList")) {
					DateHelper mDateHelper = new DateHelper(
							text_voice_camera_date_bar_dateview.getText()
									.toString());
					_list.put(DatabaseAdapter.KEY_DATE_TIME,
							mDateHelper.getTimeMillis() + "");
				} else {
					if(!intentExtras.containsKey("timeInMillis")){
						DateHelper mDateHelper = new DateHelper(text_voice_camera_date_bar_dateview.getText().toString());
						_list.put(DatabaseAdapter.KEY_DATE_TIME, mDateHelper.getTimeMillis()+"");
					} else {
						Calendar mCalendar = Calendar.getInstance();
						mCalendar.setTimeInMillis(intentExtras.getLong("timeInMillis"));
						DateHelper mDateHelper = new DateHelper(text_voice_camera_date_bar_dateview.getText().toString(),mCalendar);
						_list.put(DatabaseAdapter.KEY_DATE_TIME, mDateHelper.getTimeMillis()+"");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(MainActivity.mCurrentLocation != null  && setLocation == true){
			if (!MainActivity.mCurrentLocation.equals("")) {
				_list.put(DatabaseAdapter.KEY_LOCATION,MainActivity.mCurrentLocation);
			}
		}
		// //// ******* Update database if user added additional info *******
		// ///////
		mDatabaseAdapter.open();
		mDatabaseAdapter.editDatabase(_list);
		mDatabaseAdapter.close();
		finish();
		if (!intentExtras.containsKey("timeInMillis")
				&& !intentExtras.containsKey("mDisplayList")) {
			Intent intentExpenseListing = new Intent(this, ExpenseListing.class);
			startActivity(intentExpenseListing);
		}
	}

	// /////// ********* CountdownTimer for Chronometer ********* //////////
	// countdowntimer is an abstract class, so extend it and fill in methods
	private class MyCount extends CountDownTimer {

		DisplayTime mDisplayTime;

		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			mDisplayTime = new DisplayTime();
		}

		@Override
		public void onFinish() {
			text_voice_camera_time_details_chronometer.setText(mDisplayTime
					.getDisplayTime(mAudioPlay.getPlayBackTime()));
			text_voice_camera_stop_button.setVisibility(View.GONE);
			text_voice_camera_play_button.setVisibility(View.VISIBLE);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			text_voice_camera_time_details_chronometer.setText(mDisplayTime
					.getDisplayTime(millisUntilFinished));
		}
	}

	// /// ****************** Handling back press of key ********** ///////////
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onBackPressed() {
		// This will be called either automatically for you on 2.0
		// or later, or by the code above on earlier versions of the platform.
		saveEntry();
		try {
			if (mAudioPlay.isAudioPlaying())
				mAudioPlay.stopPlayBack();
		} catch (Exception e) {
		}
		return;
	}
}
