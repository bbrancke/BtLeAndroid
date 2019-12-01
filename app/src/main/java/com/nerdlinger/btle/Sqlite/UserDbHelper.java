package com.nerdlinger.btle.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nerdlinger.btle.User.OneUser;

public class UserDbHelper extends SQLiteOpenHelper {
	private static final String[] COLUMNS = {
			DbContract.User._ID,
			DbContract.User.COLUMN_NAME_USERNAME
	};

	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + DbContract.User.TABLE_NAME + " (" +
					DbContract.User._ID + " INTEGER PRIMARY KEY," +
					DbContract.User.COLUMN_NAME_USERNAME + " TEXT);";

	private static final String SQL_DELETE_TABLE =
			"DROP TABLE IF EXISTS " + DbContract.User.TABLE_NAME +";";

	public UserDbHelper(Context context) {
		super(context, DbContract.DbInfo.DATABASE_FILENAME, null, DbContract.DbInfo. DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_TABLE);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	public long InsertUser(OneUser user) {
		long id;
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		// Only ONE user:
		db.execSQL("DELETE FROM " + DbContract.User.TABLE_NAME + ";");

		values.put(DbContract.User.COLUMN_NAME_USERNAME, user.GetUsername());

		id = db.insert(DbContract.User.TABLE_NAME, null, values);
		db.close();
		return id;
	}

	public OneUser GetUser() {
		OneUser user = null;
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(DbContract.User.TABLE_NAME,
				COLUMNS,
				null,
				null,
				null, // e. group by
				null, // f. having
				null, // g. order by
				null); // h. limit

		if (cursor.moveToFirst()) {
			user = new OneUser(
					Integer.parseInt(cursor.getString(0)),  // Id
					cursor.getString(1));  // Name
		}
		cursor.close();
		db.close();
		return user;
	}
}
