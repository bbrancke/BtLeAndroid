package com.nerdlinger.btle.ui;

import java.util.HashMap;

public class UuidLookup {
    private HashMap<String, String> m_lookups = new HashMap<>();

    public final String SvcId_GlucoseService = MakeFullUuid("1808");

    // These are characteristics of the Glucose Service:
    // "ChrId": Characteristic ID
    // GlugoseMeasurement, GlugoseMeasurementContext, GlucoseFeature and RACP are all
    //   "Glucose Service" characteristics; I haven't investigated the
    //   other service's characteristics yet...
    public final String ChrId_GlucoseMeasurement = MakeFullUuid("2a18");  // "Glucose Measurement"
    public final String ChrId_GlucoseMeasurementContext = MakeFullUuid("2a34");  // "Glucose Measurement Context");
    public final String ChrId_GlucoseFeature = MakeFullUuid("2a51");
    public final String ChrId_RecordAccessControlPoint = MakeFullUuid("2a52");

    // Descriptor attached to ("behind") 2a18, 2a34, 2a52:
    // "DscId": Descriptor ID
    public final String DscId_ClientConfigurationConfig = MakeFullUuid("2902");

    private String MakeFullUuid(String shortValue) {
        String val = "0000" + shortValue + "-0000-1000-8000-00805f9b34fb";
        return val.toLowerCase();
    }

    public UuidLookup() {
        // UUIDs:
        // To reconstruct the full 128-bit UUID from the shortened version, insert the 16- or 32-bit short value (indicated by xxxxxxxx, including leading zeros) into the Bluetooth Base UUID:
        //
        // xxxxxxxx-0000-1000-8000-00805F9B34FB
        //         +-------------------------+ BT Base UUID
        // [Vendor-specific UUIDs do NOT have to "derive" from BT Base UUID,
        //    these won't have the short (hex) number.]
        // Services:
        //   https://developer.bluetooth.org/gatt/services/Pages/ServicesHome.aspx
        m_lookups.put(MakeFullUuid("1800"), "Generic Access");
        m_lookups.put(MakeFullUuid("1801"), "Generic Attribute");
        m_lookups.put(MakeFullUuid("1802"), "Immediate Alert");
        m_lookups.put(MakeFullUuid("1803"), "Link Loss");
        m_lookups.put(MakeFullUuid("1804"), "Tx Power");
        m_lookups.put(MakeFullUuid("1805"), "Current Time Service");
        m_lookups.put(MakeFullUuid("1806"), "Reference Time Update Service");
        m_lookups.put(MakeFullUuid("1807"), "Next DST Change");
        m_lookups.put(SvcId_GlucoseService /* "1808" */, "Glucose Service");
        m_lookups.put(MakeFullUuid("1809"), "Health Thermometer");
        m_lookups.put(MakeFullUuid("180d"), "Heart Rate Service");
        m_lookups.put(MakeFullUuid("180e"), "Phone Alert Status");
        m_lookups.put(MakeFullUuid("180f"), "Battery");
        m_lookups.put(MakeFullUuid("180a"), "Device Information Service");
        m_lookups.put(MakeFullUuid("1810"), "Blood Pressure");
        m_lookups.put(MakeFullUuid("1811"), "Alert Notification");
        m_lookups.put(MakeFullUuid("1812"), "Human Interface Device");
        m_lookups.put(MakeFullUuid("1813"), "Scan Parameters");
        m_lookups.put(MakeFullUuid("1814"), "Running Speed and Cadence");
        m_lookups.put(MakeFullUuid("1816"), "Cycling Speed and Cadence");
        m_lookups.put(MakeFullUuid("ffe0"), "Simple Key Service");
        m_lookups.put(MakeFullUuid("fff0"), "Soft Serial Service");

        // Characteristics:
        //   https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicsHome.aspx
        m_lookups.put(ChrId_GlucoseMeasurement /* 2a18 */, "Glucose Measurement");
        m_lookups.put(ChrId_GlucoseMeasurementContext /* 2a34 */, "Glucose Measurement Context");
        m_lookups.put(MakeFullUuid("2a37"), "Heart Rate Measurement");
        m_lookups.put(MakeFullUuid("2a29"), "Manufacturer Name String");
        m_lookups.put(ChrId_GlucoseFeature /* 2a51 */, "Glucose Feature");
        m_lookups.put(ChrId_RecordAccessControlPoint /* 2a52 */, "Record Access Control Point");

        m_lookups.put(DscId_ClientConfigurationConfig /* 2902 */, "Client Characteristic Config");
/*
TODO: Finish this off for other BT LE devices
Time consuming, for now only entering what I think will need...
        AlertCategoryID = 0x2A43,
        AlertCategoryIDBitMask = 0x2A42,
        AlertLevel = 0x2A06,
        AlertNotificationControlPoint = 0x2A44,
        AlertStatus = 0x2A3F,
        Appearance = 0x2A01,
        BatteryLevel = 0x2A19,
        BloodPressureFeature = 0x2A49,
        BloodPressureMeasurement = 0x2A35,
        BodySensorLocation = 0x2A38,
        BootKeyboardInputReport = 0x2A22,
        BootKeyboardOutputReport = 0x2A32,
        BootMouseInputReport = 0x2A33,
        CSCFeature = 0x2A5C,
        CSCMeasurement = 0x2A5B,
        CurrentTime = 0x2A2B,
        DateTime = 0x2A08,
        DayDateTime = 0x2A0A,
        DayofWeek = 0x2A09,
        DeviceName = 0x2A00,
        DSTOffset = 0x2A0D,
        ExactTime256 = 0x2A0C,
        FirmwareRevisionString = 0x2A26,
        GlucoseFeature = 0x2A51,
        GlucoseMeasurement = 0x2A18,
        GlucoseMeasurementContext = 0x2A34,
        HardwareRevisionString = 0x2A27,
        HeartRateControlPoint = 0x2A39,
        HeartRateMeasurement = 0x2A37,
        HIDControlPoint = 0x2A4C,
        HIDInformation = 0x2A4A,
        IEEE11073_20601RegulatoryCertificationDataList = 0x2A2A,
        IntermediateCuffPressure = 0x2A36,
        IntermediateTemperature = 0x2A1E,
        LocalTimeInformation = 0x2A0F,
        ManufacturerNameString = 0x2A29,
        MeasurementInterval = 0x2A21,
        ModelNumberString = 0x2A24,
        NewAlert = 0x2A46,
        PeripheralPreferredConnectionParameters = 0x2A04,
        PeripheralPrivacyFlag = 0x2A02,
        PnPID = 0x2A50,
        ProtocolMode = 0x2A4E,
        ReconnectionAddress = 0x2A03,
        RecordAccessControlPoint = 0x2A52,
        ReferenceTimeInformation = 0x2A14,
        Report = 0x2A4D,
        ReportMap = 0x2A4B,
        RingerControlPoint = 0x2A40,
        RingerSetting = 0x2A41,
        RSCFeature = 0x2A54,
        RSCMeasurement = 0x2A53,
        SCControlPoint = 0x2A55,
        ScanIntervalWindow = 0x2A4F,
        ScanRefresh = 0x2A31,
        SensorLocation = 0x2A5D,
        SerialNumberString = 0x2A25,
        ServiceChanged = 0x2A05,
        SoftwareRevisionString = 0x2A28,
        SupportedNewAlertCategory = 0x2A47,
        SupportedUnreadAlertCategory = 0x2A48,
        SystemID = 0x2A23,
        TemperatureMeasurement = 0x2A1C,
        TemperatureType = 0x2A1D,
        TimeAccuracy = 0x2A12,
        TimeSource = 0x2A13,
        TimeUpdateControlPoint = 0x2A16,
        TimeUpdateState = 0x2A17,
        TimewithDST = 0x2A11,
        TimeZone = 0x2A0E,
        TxPowerLevel = 0x2A07,
        UnreadAlertStatus = 0x2A45,
        AggregateInput = 0x2A5A,
        AnalogInput = 0x2A58,
        AnalogOutput = 0x2A59,
        CyclingPowerControlPoint = 0x2A66,
        CyclingPowerFeature = 0x2A65,
        CyclingPowerMeasurement = 0x2A63,
        CyclingPowerVector = 0x2A64,
        DigitalInput = 0x2A56,
        DigitalOutput = 0x2A57,
        ExactTime100 = 0x2A0B,
        LNControlPoint = 0x2A6B,
        LNFeature = 0x2A6A,
        LocationandSpeed = 0x2A67,
        Navigation = 0x2A68,
        NetworkAvailability = 0x2A3E,
        PositionQuality = 0x2A69,
        ScientificTemperatureinCelsius = 0x2A3C,
        SecondaryTimeZone = 0x2A10,
        String = 0x2A3D,
        TemperatureinCelsius = 0x2A1F,
        TemperatureinFahrenheit = 0x2A20,
        TimeBroadcast = 0x2A15,
        BatteryLevelState = 0x2A1B,
        BatteryPowerState = 0x2A1A,
        PulseOximetryContinuousMeasurement = 0x2A5F,
        PulseOximetryPulsatileEvent = 0x2A60,
        PulseOximetryFeatures = 0x2A61,
        PulseOximetryControlPoint = 0x2A62,
        SimpleKeyState = 0xFFE1
 */
    }

    public String lookup(String uuid) {
        String val = uuid.toLowerCase();
        String name = m_lookups.get(val);
        if (name != null) {
            return name;
        }
        // else..
        return "(unknown)";
    }

    public String GetNameWithShortUuid(String uuid) {
        String val = uuid.toLowerCase();
        String name = m_lookups.get(val);
        if (name == null) {
            return "(unknown): " + uuid;
        }
        // (else...)
        // 01234567
        // 00001234-0000-1000-8000-00805F9B34FB
        String shortUuid;
        if (val.length() > 7) {
            shortUuid = " (0x" + val.substring(4, 8) + ")";
        }
        else {
            shortUuid = " (0x" + val + ")";
        }
        return name + shortUuid;
    }
}
