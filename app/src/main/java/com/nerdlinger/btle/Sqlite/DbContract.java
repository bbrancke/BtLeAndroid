package com.nerdlinger.btle.Sqlite;

public final class DbContract {
    private DbContract() {}

    public static class DbInfo {
        // If you change the database schema, increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "glucose_readings_bt.db";
    }
}
