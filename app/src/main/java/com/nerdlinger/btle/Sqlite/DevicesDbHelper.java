package com.nerdlinger.btle.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nerdlinger.btle.BleDevices.OneDevice;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class DevicesDbHelper extends SQLiteOpenHelper {
    private static final String[] COLUMNS = {
            DevicesContract.DevicesBt._ID,
            DevicesContract.DevicesBt.COLUMN_NAME_DEVICE_NAME,
            DevicesContract.DevicesBt.COLUMN_NAME_DEVICE_BDADDR,
            DevicesContract.DevicesBt.COLUMN_NAME_DISCOVERED,
            DevicesContract.DevicesBt.COLUMN_NAME_TOTALREADINGS,
            DevicesContract.DevicesBt.COLUMN_NAME_LASTSEEN,
            DevicesContract.DevicesBt.COLUMN_NAME_LASTREADINGS
    };

    public DevicesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    // If you change the database schema, increment the database version.
    public static final int DATABASE_VERSION = DbContract.DbInfo.DATABASE_VERSION;
    public static final String DATABASE_NAME = DbContract.DbInfo.DATABASE_NAME;
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DevicesContract.DevicesBt.TABLE_NAME + " (" +
                    DevicesContract.DevicesBt._ID + " INTEGER PRIMARY KEY," +
                    DevicesContract.DevicesBt.COLUMN_NAME_DEVICE_NAME + " TEXT," +
                    DevicesContract.DevicesBt.COLUMN_NAME_DEVICE_BDADDR + " TEXT, " +
                    DevicesContract.DevicesBt.COLUMN_NAME_DISCOVERED + " TEXT," +
                    DevicesContract.DevicesBt.COLUMN_NAME_TOTALREADINGS + " INTEGER," +
                    DevicesContract.DevicesBt.COLUMN_NAME_LASTSEEN + " TEXT," +
                    DevicesContract.DevicesBt.COLUMN_NAME_LASTREADINGS + " INTEGER)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + DevicesContract.DevicesBt.TABLE_NAME;

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

    public void InsertDevice(OneDevice device) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DevicesContract.DevicesBt.COLUMN_NAME_DEVICE_NAME, device.GetName());
        values.put(DevicesContract.DevicesBt.COLUMN_NAME_DEVICE_BDADDR, device.GetBdaddr());
        values.put(DevicesContract.DevicesBt.COLUMN_NAME_DISCOVERED, device.GetDiscoveredOn());
        values.put(DevicesContract.DevicesBt.COLUMN_NAME_TOTALREADINGS, device.GetTotalReadings());
        values.put(DevicesContract.DevicesBt.COLUMN_NAME_LASTSEEN, device.GetLastSeen());
        values.put(DevicesContract.DevicesBt.COLUMN_NAME_LASTREADINGS, device.GetLastSeenCount());
        db.insert(DevicesContract.DevicesBt.TABLE_NAME, null, values);
        db.close();
    }

    public List<OneDevice> GetBtDevices() {
        List<OneDevice> devices = new LinkedList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(DevicesContract.DevicesBt.TABLE_NAME,
            COLUMNS,
            null,
            null,
            null, // e. group by
            null, // f. having
            null, // g. order by
            null); // h. limit

        if (cursor.moveToFirst()) {
            do {
                OneDevice dev = new OneDevice(
                        Integer.parseInt(cursor.getString(0)),  // Id
                        cursor.getString(1),  // Name
                        cursor.getString(2),  // BDADDR
                        cursor.getString(3),  // Discovered On
                        Integer.parseInt(cursor.getString(4)),  // Total Readings
                        cursor.getString(5),  // Last Seen
                        Integer.parseInt(cursor.getString(6)));  // Last Seen Count
                devices.add(dev);
            } while (cursor.moveToNext());
        }

        db.close();
        return devices;
    }

}
