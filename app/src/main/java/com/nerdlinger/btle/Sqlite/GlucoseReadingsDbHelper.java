package com.nerdlinger.btle.Sqlite;

import com.nerdlinger.btle.GlucoseReading.OneReading;
import com.nerdlinger.btle.Sqlite.DbContract;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDateTime;

// To access the database, instantiate your subclass of SQLiteOpenHelper:
//   GlucoseReadingsDbHelper dbHelper = new GlucoseReadingsDbHelper(getContext());
// Insert data into the database by passing a ContentValues object to the insert() method:
//   Gets the data repository in write mode:
//   SQLiteDatabase db = dbHelper.getWritableDatabase();
// Create a new map of values, where column names are the keys
//   ContentValues values = new ContentValues();
//   values.put(FeedEntry.COLUMN_NAME_TITLE, title);
//   values.put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);
// Insert the new row, returning the primary key value of the new row
//   long newRowId = db.insert(GlucoseReadingsBt.TABLE_NAME, null, values);
//
// Date/Time:
// TEXT as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
// LocalDateTime.of(1994, Month.APRIL, 15, 11, 30)); (String) " 1994-04-15T11:30"
//        LocalDateTime.of(2019, 6, 11, 15, 42, 15).toString();
//        LocalDateTime.parse("2019-06-11T15:42:15");

public class GlucoseReadingsDbHelper extends SQLiteOpenHelper {

	private static final String[] COLUMNS = {
			DbContract.GlucoseReadingsBt._ID,
			DbContract.GlucoseReadingsBt.COLUMN_NAME_DEVICE_ID,
			DbContract.GlucoseReadingsBt.COLUMN_NAME_RAW_DATA,
			DbContract.GlucoseReadingsBt.COLUMN_NAME_SEQUENCE_NUMBER,
			DbContract.GlucoseReadingsBt.COLUMN_NAME_READING_TAKEN_AT,
			DbContract.GlucoseReadingsBt.COLUMN_NAME_GLUCOSE_READING
	};
	public GlucoseReadingsDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static final int DATABASE_VERSION = DbContract.DbInfo.DATABASE_VERSION;
	public static final String DATABASE_NAME = DbContract.DbInfo.DATABASE_FILENAME;

	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + DbContract.GlucoseReadingsBt.TABLE_NAME + " (" +
					DbContract.GlucoseReadingsBt._ID + " INTEGER PRIMARY KEY," +
					DbContract.GlucoseReadingsBt.COLUMN_NAME_DEVICE_ID + " INTEGER," +
					DbContract.GlucoseReadingsBt.COLUMN_NAME_RAW_DATA + " TEXT," +
					DbContract.GlucoseReadingsBt.COLUMN_NAME_SEQUENCE_NUMBER + " INTEGER, " +
					DbContract.GlucoseReadingsBt.COLUMN_NAME_READING_TAKEN_AT + " TEXT," +
					DbContract.GlucoseReadingsBt.COLUMN_NAME_GLUCOSE_READING + " INTEGER);";
	// CREATE [UNIQUE] INDEX index_name ON table_name(indexed_column);
	private static final String SQL_CREATE_INDEXES =
			"CREATE INDEX IDX_WHEN ON " +
					DbContract.GlucoseReadingsBt.TABLE_NAME +
					"(" + DbContract.GlucoseReadingsBt.COLUMN_NAME_READING_TAKEN_AT + ");";

	private static final String SQL_DELETE_TABLE =
			"DROP TABLE IF EXISTS " + DbContract.GlucoseReadingsBt.TABLE_NAME;


	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(SQL_CREATE_ENTRIES);
		db.execSQL(SQL_CREATE_INDEXES);
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

	public void InsertReading(OneReading reading) {
		SQLiteDatabase db = getWritableDatabase();
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(DbContract.GlucoseReadingsBt.COLUMN_NAME_DEVICE_ID, reading.GetDeviceId());
		values.put(DbContract.GlucoseReadingsBt.COLUMN_NAME_RAW_DATA, reading.GetRawDataString());
		values.put(DbContract.GlucoseReadingsBt.COLUMN_NAME_SEQUENCE_NUMBER, reading.GetSequenceNumber());
		values.put(DbContract.GlucoseReadingsBt.COLUMN_NAME_READING_TAKEN_AT, reading.GetTimeStamp());
		values.put(DbContract.GlucoseReadingsBt.COLUMN_NAME_GLUCOSE_READING, reading.GetMeasurement());
		// Insert the new row, returning the primary key value of the new row
		//   long newRowId = db.insert(GlucoseReadingsBt.TABLE_NAME, null, values);
		db.insert(DbContract.GlucoseReadingsBt.TABLE_NAME, null, values);
		db.close();
	}
	/*******************************************************
	 public List<Book> getAllBooks() {
	 List<Book> books = new LinkedList<Book>();

	 // 1. build the query
	 String query = "SELECT  * FROM " + TABLE_BOOKS;
	 // I don't like the '*' - put in what we want then we know col #0, #1, #2 are explicitly...
	 // 2. get reference to writable DB
	 SQLiteDatabase db = this.getWritableDatabase();
	 Cursor cursor = db.rawQuery(query, null);

	 // 3. go over each row, build book and add it to list
	 Book book = null;
	 if (cursor.moveToFirst()) {
	 do {
	 book = new Book();
	 book.setId(Integer.parseInt(cursor.getString(0)));
	 book.setTitle(cursor.getString(1));
	 book.setAuthor(cursor.getString(2));
	 // Add book to books
	 books.add(book);
	 } while (cursor.moveToNext());
	 }
	 Log.d("getAllBooks()", books.toString());
	 return books;
	 }
	 or
	 Cursor cursor =
	 db.query(TABLE_BOOKS, // a. table
	 COLUMNS, // b. column names
	 " id = ?", // c. selections
	 new String[] { String.valueOf(id) }, // d. selections args
	 null, // e. group by
	 null, // f. having
	 null, // g. order by
	 null); // h. limit
	 if (cursor .moveToFirst()) { get data ...} / moveToNext() , as above
	 ********************************************************/

}
