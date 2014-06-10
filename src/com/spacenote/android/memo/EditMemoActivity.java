package com.spacenote.android.memo;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.azazeleleven.android.memo.DaoMaster;
import com.azazeleleven.android.memo.DaoMaster.DevOpenHelper;
import com.azazeleleven.android.memo.DaoSession;
import com.azazeleleven.android.memo.Note;
import com.azazeleleven.android.memo.NoteDao;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class EditMemoActivity extends SherlockActivity implements OnClickListener {
	TimePicker myTimePicker;
	TimePickerDialog timePickerDialog;

	private EditText editText;
	private String memoText;
	private String alarmTime;

	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private NoteDao memoDao;
	private SQLiteDatabase db;
	private Long mRowId;

	private Calendar newCal;
	private Note memo;
	private LinearLayout llTimerButtonHost;


	private Date currentTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_memo);

//		currentTime = Calendar.getInstance().getTime();

		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "memo-db",
				null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		memoDao = daoSession.getNoteDao();

		editText = (EditText) findViewById(R.id.edit_text_memo);

	// Get extras from list activity
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String memoText = extras.getString("memoText");
			mRowId = extras.getLong("mRowId");
			alarmTime = extras.getString("alarmTime");
			currentTime = (Date) extras.get("currentTime");
			if (memoText != null && mRowId != null) {
				editText.setText(memoText);
				// mCurSpinnerPos = 0;
				if (currentTime != null) {
					if (alarmTime != null) {
						// only show the cancel timer button if an alarm was
						// previously set
						llTimerButtonHost.setVisibility(View.VISIBLE);
					}
				}
			}
		}

		// Inflate a "Done/Discard" custom action bar view.
		LayoutInflater inflater = (LayoutInflater) getSupportActionBar()
				.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		final View customActionBarView = inflater.inflate(
				R.layout.actionbar_custom_view_done_discard, null);
		customActionBarView.findViewById(R.id.actionbar_done)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						memoText = editText.getText().toString();
						// notify user if edit text is empty
						if (memoText.matches("")) {
							Crouton.makeText(EditMemoActivity.this,
									R.string.toast_input_required, Style.ALERT,
									R.id.alternate_view_group).show();
							return;						
						} else {
							saveMemoText();
							finish();
						}
					}
				});
		customActionBarView.findViewById(R.id.actionbar_discard)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});

		// Show the custom action bar view and hide the normal Home icon and
		// title.
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
		getSupportActionBar().setCustomView(customActionBarView,
				new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

		// // Get Note to Self intent
		String msg = getIntent().getStringExtra("android.intent.extra.TEXT");
		if (msg == null) {
			return;
		} else {
			memoText = msg;
			currentTime = Calendar.getInstance().getTime();
			mRowId = null;
			saveMemoText();
			
		}

	}

	@Override
	public void onClick(View v) {
		memoText = editText.getText().toString();

	}

	// save the text that was entered
	private void saveMemoText() {
		alarmTime = null;
		currentTime = null;
		memo = new Note(mRowId, memoText, alarmTime, currentTime);
		memo.setComment(alarmTime);
		memo.setDate(currentTime);
		memo.setId(mRowId);
		memoDao.insertOrReplace(memo);
		db.close();
	}

	@Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

}
