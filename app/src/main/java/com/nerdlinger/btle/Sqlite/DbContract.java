package com.nerdlinger.btle.Sqlite;

import android.provider.BaseColumns;

public final class DbContract {
    private DbContract() {}

    public static class DbInfo {
        // If you change the database schema, increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_FILENAME = "glucose_readings_bt.db";
    }

    // Devices table columns:
    public static class DevicesBt implements BaseColumns {
        public static final String TABLE_NAME = "Devices";

        public static final String COLUMN_NAME_DEVICE_NAME = "Name";
        public static final String COLUMN_NAME_DEVICE_BDADDR = "Bdaddr";
        public static final String COLUMN_NAME_DISPLAY_NAME = "DisplayName";
        public static final String COLUMN_NAME_DEVICE_TYPE = "Type";
        public static final String COLUMN_NAME_ISACTIVE = "IsActive";
        public static final String COLUMN_NAME_DISCOVERED = "Discovered";
        public static final String COLUMN_NAME_TOTALREADINGS = "TotalReadings";
        public static final String COLUMN_NAME_LASTSEEN = "LastSeen";
        public static final String COLUMN_NAME_LASTREADINGS = "LastReadingsCount";
    }

// Glucose Readings table contents:
    public static class GlucoseReadingsBt implements BaseColumns {
        public static final String TABLE_NAME = "glucose_readings";
        // OLD TABLE:
        // "CREATE TABLE glucose_readings "
        //   (grId INTEGER PRIMARY KEY, reading_taken_at REAL, glucose_reading INTEGER);";
        // NEW: This one has:
        //     _id INTEGER PK, device_id INTEGER, raw_data TEXT, sequece_number INTEGER,
        //     reading_taken_at TEXT, glucose_reading INTEGER
        public static final String COLUMN_NAME_DEVICE_ID = "device_id";
        public static final String COLUMN_NAME_RAW_DATA = "raw_data";
        public static final String COLUMN_NAME_SEQUENCE_NUMBER = "sequence_number";
        public static final String COLUMN_NAME_READING_TAKEN_AT = "reading_taken_at";
        public static final String COLUMN_NAME_GLUCOSE_READING = "glucose_reading";
    }

	// Users table columns:
	public static class User implements BaseColumns {
		public static String TABLE_NAME = "Users";

		public static String COLUMN_NAME_USERNAME = "UserName";
	}
}
