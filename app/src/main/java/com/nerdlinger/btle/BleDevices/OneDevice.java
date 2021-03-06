package com.nerdlinger.btle.BleDevices;

import com.nerdlinger.btle.Utilities.HexConverter;

import java.time.LocalDateTime;

public class OneDevice {
    /*
      public static final String COLUMN_NAME_DEVICE_NAME = "Name";
        public static final String COLUMN_NAME_DEVICE_BDADDR = "Bdaddr";
       	public static final String COLUMN_NAME_DISPLAY_NAME = "DisplayName";
        public static final String COLUMN_NAME_DEVICE_TYPE = "Type";
        public static final String COLUMN_NAME_ISACTIVE = "IsActive";
        public static final String COLUMN_NAME_DISCOVERED = "Discovered";
        public static final String COLUMN_NAME_TOTALREADINGS = "TotalReadings";
        public static final String COLUMN_NAME_LASTSEEN = "LastSeen";
        public static final String COLUMN_NAME_LASTREADINGS = "LastReadingsCount";
     */
    

    private long m_id;  // SQL Row Id

    public long GetId() { return m_id; }
	public void SetId(long id) { m_id = id; }

    private String m_name;
    public String GetName() { return m_name; }

    private String m_bdaddr;
    public String GetBdaddr() { return m_bdaddr; }

    private String m_displayName;
    public String GetDisplayName() { return m_displayName; }

    private int m_deviceType;
    public int GetDeviceType() { return m_deviceType; }

    private boolean m_isActive;
    public boolean GetIsActive() { return m_isActive; }

    private String m_discoveredOn;
    public String GetDiscoveredOn() { return m_discoveredOn; }

    private int m_totalReadings;
    public int GetTotalReadings() { return m_totalReadings;}

    private String m_lastSeen;
    public String GetLastSeen() { return m_lastSeen; }

    private int m_lastSeenCount;
    public int GetLastSeenCount() { return m_lastSeenCount; }

    // Reading devices from SQL:
    public OneDevice(long id, String name, String bdaddr, String displayName,
                     int deviceType, boolean isActive,
                     String discoveredOn, int totalReadings, String lastSeen, int lastSeenCount) {
        m_id = id;  // SQL Row Id
        m_name = name;
        m_bdaddr = bdaddr;
        m_displayName = displayName;
        m_deviceType = deviceType;
        m_isActive = isActive;
        m_discoveredOn = discoveredOn;
        m_totalReadings = totalReadings;
        m_lastSeen = lastSeen;
        m_lastSeenCount = lastSeenCount;
    }

    // When we discover a new device:
    public OneDevice(String name, String bdaddr) {
        m_id = 0;
        m_name = name;
        m_bdaddr = bdaddr;
        //LocalDateTime dt;
        // NO 'Z' at the end; 'Z' throws an exception
        //   It is after all LOCAL DateTime...
        //dt = LocalDateTime.parse("2019-07-06T07:05:04");
        LocalDateTime dt = LocalDateTime.now();
        m_discoveredOn = dt.toString();
        m_totalReadings = 0;
        m_lastSeen = m_discoveredOn;
        m_lastSeenCount = 0;
    }

    private void blah() {
        LocalDateTime dt;
        int year = 2019;
        int month = 07;
        int day = 06;
        int hour = 06;
        int minute = 12;
        int sec = 00;

        dt = LocalDateTime.of(year, month, day, hour, minute, sec);
        dt = LocalDateTime.parse("2019-07-06T07:05:04");  // *NO* 'Z' at the end!
    }

}
