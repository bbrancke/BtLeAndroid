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
            DbContract.DevicesBt._ID,
            DbContract.DevicesBt.COLUMN_NAME_DEVICE_NAME,
            DbContract.DevicesBt.COLUMN_NAME_DEVICE_BDADDR,
            DbContract.DevicesBt.COLUMN_NAME_DISPLAY_NAME,
            DbContract.DevicesBt.COLUMN_NAME_DEVICE_TYPE,
            DbContract.DevicesBt.COLUMN_NAME_ISACTIVE,
            DbContract.DevicesBt.COLUMN_NAME_DISCOVERED,
            DbContract.DevicesBt.COLUMN_NAME_TOTALREADINGS,
            DbContract.DevicesBt.COLUMN_NAME_LASTSEEN,
            DbContract.DevicesBt.COLUMN_NAME_LASTREADINGS
    };

    public DevicesDbHelper(Context context) {
        super(context, DbContract.DbInfo.DATABASE_FILENAME,null, DbContract.DbInfo.DATABASE_VERSION);
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DbContract.DevicesBt.TABLE_NAME + " (" +
                    DbContract.DevicesBt._ID + " INTEGER PRIMARY KEY," +
                    DbContract.DevicesBt.COLUMN_NAME_DEVICE_NAME + " TEXT," +
                    DbContract.DevicesBt.COLUMN_NAME_DEVICE_BDADDR + " TEXT, " +
                    DbContract.DevicesBt.COLUMN_NAME_DISPLAY_NAME + " TEXT, " +
                    DbContract.DevicesBt.COLUMN_NAME_DEVICE_TYPE + " INTEGER, " +
                    DbContract.DevicesBt.COLUMN_NAME_ISACTIVE + " INTEGER, " +
                    DbContract.DevicesBt.COLUMN_NAME_DISCOVERED + " TEXT," +
                    DbContract.DevicesBt.COLUMN_NAME_TOTALREADINGS + " INTEGER," +
                    DbContract.DevicesBt.COLUMN_NAME_LASTSEEN + " TEXT," +
                    DbContract.DevicesBt.COLUMN_NAME_LASTREADINGS + " INTEGER);";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + DbContract.DevicesBt.TABLE_NAME + ";";

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

    public long InsertDevice(OneDevice device) {
        long id;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.DevicesBt.COLUMN_NAME_DEVICE_NAME, device.GetName());
        values.put(DbContract.DevicesBt.COLUMN_NAME_DEVICE_BDADDR, device.GetBdaddr());
        values.put(DbContract.DevicesBt.COLUMN_NAME_DISPLAY_NAME, device.GetDisplayName());
        values.put(DbContract.DevicesBt.COLUMN_NAME_DEVICE_TYPE, device.GetDeviceType());
        values.put(DbContract.DevicesBt.COLUMN_NAME_ISACTIVE, device.GetIsActive() ? 1 : 0);
        values.put(DbContract.DevicesBt.COLUMN_NAME_DISCOVERED, device.GetDiscoveredOn());
        values.put(DbContract.DevicesBt.COLUMN_NAME_TOTALREADINGS, device.GetTotalReadings());
        values.put(DbContract.DevicesBt.COLUMN_NAME_LASTSEEN, device.GetLastSeen());
        values.put(DbContract.DevicesBt.COLUMN_NAME_LASTREADINGS, device.GetLastSeenCount());
        id = db.insert(DbContract.DevicesBt.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public List<OneDevice> GetBtDevices() {
        List<OneDevice> devices = new LinkedList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(DbContract.DevicesBt.TABLE_NAME,
            COLUMNS,
            null,
            null,
            null, // e. group by
            null, // f. having
            null, // g. order by
            null); // h. limit

        if (cursor.moveToFirst()) {
            do {
                /*
                OneDevice(int id, String name, String bdaddr, String displayName,
                     int deviceType, boolean isActive,
                     String discoveredOn, int totalReadings, String lastSeen, int lastSeenCount)
                */
                int active = Integer.parseInt(cursor.getString(5));
                OneDevice dev = new OneDevice(
                        Integer.parseInt(cursor.getString(0)),  // Id
                        cursor.getString(1),  // Name
                        cursor.getString(2),  // BDADDR
                        cursor.getString(3),  // DisplayName
                        Integer.parseInt(cursor.getString(4)),  // deviceType (int)
                        (active == 1),  // IsActive (bool)
                        cursor.getString(6),  // Discovered On
                        Integer.parseInt(cursor.getString(7)),  // Total Readings
                        cursor.getString(8),  // Last Seen
                        Integer.parseInt(cursor.getString(9)));  // Last Seen Count
                devices.add(dev);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return devices;
    }

}
