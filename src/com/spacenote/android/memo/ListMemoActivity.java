package com.spacenote.android.memo;

import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.azazeleleven.android.memo.DaoMaster;
import com.azazeleleven.android.memo.DaoMaster.DevOpenHelper;
import com.azazeleleven.android.memo.DaoSession;
import com.azazeleleven.android.memo.Note;
import com.azazeleleven.android.memo.NoteDao;

import de.timroes.swipetodismiss.SwipeDismissList;
import de.timroes.swipetodismiss.SwipeDismissList.Undoable;

public class ListMemoActivity extends SherlockListActivity {

	public static final String TAG = "memoapp";
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private NoteDao memoDao;

	private Cursor cursor;

	private SQLiteDatabase db;
	private String alarmTime;
	private long mRowId;
	private Note memo;
	private String memoText;
	protected Date currentTimeRequestCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_memo);

		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "memo-db",
				null);
		db = helper.getReadableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		memoDao = daoSession.getNoteDao();

		final String textColumn = NoteDao.Properties.Text.columnName;
		alarmTime = NoteDao.Properties.Comment.columnName;

		cursor = db.query(memoDao.getTablename(), memoDao.getAllColumns(),
				null, null, null, null, null);
		String[] from = { textColumn, alarmTime };
		int[] to = { android.R.id.text1, android.R.id.text2 };

		@SuppressWarnings("deprecation")
		final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, cursor, from, to);
		setListAdapter(adapter);

		registerForContextMenu(getListView());

		new SwipeDismissList(getListView(),
				new SwipeDismissList.OnDismissCallback() {

					@SuppressWarnings("deprecation")
					public Undoable onDismiss(AbsListView listView,
							final int position) {
						// make sure cursor exists
						if (cursor.moveToPosition(position)) {
							memoText = cursor.getString(cursor
									.getColumnIndexOrThrow("TEXT"));
							alarmTime = cursor.getString(cursor
									.getColumnIndexOrThrow("COMMENT"));
							mRowId = adapter.getItemId(position);
							memo = memoDao.loadByRowId(mRowId);
							currentTimeRequestCode = memo.getDate();
						}

						memoDao.deleteByKey(mRowId);
						cursor.requery();
						return new SwipeDismissList.Undoable() {

							@Override
							public void undo() {
								memo = new Note(mRowId, memoText,
										alarmTime, currentTimeRequestCode);
								try {
									memoDao.insertOrReplace(memo);
								} catch (Exception e) {
									Log.e("MEMO", "exception", e);
									e.printStackTrace();
								}
								cursor.requery();
							}

							@Override
							public String getTitle() {
								return memoText + " deleted";
							}

							@Override
							public void discard() {
								memoDao.deleteByKey(mRowId);
								cursor.requery();

							}
						};

					}
				}

		);

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		cursor.requery();
		super.onResume();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		cursor.moveToPosition(position);

		Note memo = memoDao.loadByRowId(id);

		Intent intent = new Intent(this, EditMemoActivity.class);
		intent.putExtra("memoText",
				cursor.getString(cursor.getColumnIndexOrThrow("TEXT")));
		intent.putExtra("mRowId", id);
		intent.putExtra("alarmTime",
				cursor.getString(cursor.getColumnIndexOrThrow("COMMENT")));
		intent.putExtra("currentTime", memo.getDate());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		MenuItem item = menu.findItem(R.id.action_add);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				addMemo(item);
				return true;
			}
		});
		return true;		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	public void addMemo(MenuItem item) {
		Intent intent = new Intent(this, EditMemoActivity.class);
		startActivity(intent);
	}

}
