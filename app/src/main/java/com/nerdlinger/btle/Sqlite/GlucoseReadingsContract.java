package com.nerdlinger.btle.Sqlite;

import android.provider.BaseColumns;

public final class GlucoseReadingsContract {
    private GlucoseReadingsContract() { }
    // Glucose Readings table contents:
    public static class GlucoseReadingsBt implements BaseColumns {
        public static final String TABLE_NAME = "glucose_readings";
        // "CREATE TABLE glucose_readings (grId INTEGER PRIMARY KEY, reading_taken_at REAL, glucose_reading INTEGER);";
        // This one has:
        //     _id INTRGRT PK, device_id INTEGER, raw_data TEXT, sequece_number INTEGER,
        //     reading_taken_at TEXT, glucose_reading INTEGER
        public static final String COLUMN_NAME_DEVICE_ID = "device_id";
        public static final String COLUMN_NAME_RAW_DATA = "raw_data";
        public static final String COLUMN_NAME_SEQUENCE_NUMBER = "sequence_number";
        public static final String COLUMN_NAME_READING_TAKEN_AT = "reading_taken_at";
        public static final String COLUMN_NAME_GLUCOSE_READING = "glucose_reading";
    }
}
