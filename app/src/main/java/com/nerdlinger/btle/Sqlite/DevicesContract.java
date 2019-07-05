package com.nerdlinger.btle.Sqlite;

import android.provider.BaseColumns;

public final class DevicesContract {
    private DevicesContract() {}

    // DevicesContract table columns:
    public static class DevicesBt implements BaseColumns {
        public static final String TABLE_NAME = "Devices";

        public static final String COLUMN_NAME_DEVICE_NAME = "Name";
        public static final String COLUMN_NAME_DEVICE_BDADDR = "Bdaddr";
        public static final String COLUMN_NAME_DISCOVERED = "Discovered";
        public static final String COLUMN_NAME_TOTALREADINGS = "TotalReadings";
        public static final String COLUMN_NAME_LASTSEEN = "LastSeen";
        public static final String COLUMN_NAME_LASTREADINGS = "LastReadingsCount";
    }
}
