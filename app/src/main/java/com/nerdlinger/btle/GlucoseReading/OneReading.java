package com.nerdlinger.btle.GlucoseReading;

import com.nerdlinger.btle.Utilities.HexConverter;

import java.time.LocalDateTime;

public class OneReading {
	private HexConverter m_hexconverter = new HexConverter();  // String <--> Byte[] array

	private long m_deviceId;
	public long GetDeviceId() {
		return m_deviceId;
	}
	public void SetDeviceId(long device_id) {
		m_deviceId = device_id;
	}

	private byte[] m_rawDataBytes = null;
	private String m_rawDataString = "";
	// Fields written to SQL:
	// String rawData, int seq_num, String readingTakenAt, float value
	public String GetRawDataString() {
		if (m_rawDataString == "") {
			if (m_rawDataBytes == null) {
				m_rawDataString = "?";
			} else {
				m_rawDataString = m_hexconverter.ByteArrayToString(m_rawDataBytes);
			}
		}
		return m_rawDataString;
	}

	private int m_sequenceNumber = 0;
	public int GetSequenceNumber() {
		return m_sequenceNumber;
	}

	private LocalDateTime m_timeStamp = null;
	public String GetTimeStamp() {
		// Outputs this date-time as a String, such as 2007-12-03T10:15:30.
		// I think this is sortable / selectable, e.g.:
		// " WHERE TimeStamp >= '2019-02-01T00:00:00'
		//     AND TimeStamp <= '2019-05-30T23:59:59'  "
		if (m_timeStamp == null) {
			return "??";
		}
		return m_timeStamp.toString();
	}

	// public int TimeOffset { get; set; }
	private int m_timeOffset = 0;
	public int GetTimeOffset() {
		return m_timeOffset;
	}

	// // This is the (normalized) "Readable Measurement", e.g. Display as "97":
	//		public int Measurement { get; set; }
	private int m_measurement = 0;
	public int GetMeasurement() {
		return m_measurement;
	}


	private int m_flags;
	private boolean m_timeOffsetPresent;
	private boolean m_hasMeasurementConcentrationTypeAndSampleLocation;
	private boolean m_unitsAre_molPerLiter;
	private boolean m_unitsAre_kgPerLiter;
	private boolean m_sensorStatusAnnunciationPresent;
	private boolean m_contextInformationFollows;

	public void SetRawData(long device_id, byte[] data) {
		m_deviceId = device_id;
		// Make a new copy of the RX buffer, which
		// will be over-written on next RX...
		m_rawDataBytes = data.clone();
	}

	public boolean Parse() {
		if (m_rawDataBytes == null) {
			// Sanity check: Not set?
			return false;
		}
		try {
			_parse();
			return true;
		}
		catch (Exception ex) {
			// probably Index out of Range...
			return false;
		}
	}

	private void _parse() {
		int index = 0;
		// 1. Flags: (8 bit) [Mandatory]
		//    Define which data fields are present in the Characteristic value
		//    Bit #:
		//      0: Time Offset Present (1 = yes)
		//         If 1, then field [C1] follows the time (requires="C1")
		//      1: Glucose Meaurement Concentration, Type and Sample Location Present
		//         If 1, requires [C2]
		//      2: Glucose Concentration Units:
		//         0: "kg/L"  - requires [C3]
		//         1: "mol/L" - requires [C4]
		//      3: Sensor Status Annunciation Present
		//         If 1, requires [C5]
		//      4: Context Information Follows
		//      5 - 7: Reserved For Future Use
		// On my Reli-On meter, flags is:
		//      Bit:       4   3 2 1 0
		//   0x0b == 0 0 0 0 _ 1 0 1 1
		//
		m_flags = m_hexconverter.getUint8(m_rawDataBytes, index);
		index++;
		m_timeOffsetPresent = (m_flags & 0x01) != 0;
		m_hasMeasurementConcentrationTypeAndSampleLocation = (m_flags & 0x02) != 0;
		m_unitsAre_molPerLiter = (m_flags & 0x04) != 0;
		m_unitsAre_kgPerLiter = !m_unitsAre_molPerLiter;
		m_sensorStatusAnnunciationPresent = (m_flags & 0x08) != 0;
		m_contextInformationFollows = (m_flags & 0x10) != 0;
		//
		// 2. Sequence Number [Mandatory] (uint16)
		//    (Little Endian) Ex: 71 00 ==> 0x0071 = 113 decimal
		m_sequenceNumber = m_hexconverter.getUint16Le(m_rawDataBytes, index);
		index += 2;
		// 3. Base Time [Mandatory] - org.bluetooth.characteristic.date_time
		//    (See characteristic_date_time.xml in "Docs And Notes")
		//                   +=============>
		//    0000  0b 87 00 e3  07 05 0e 04
		//    0008  05 0d 74 00  5d b0 11 00
		//         <====+, length = 7.
		int year = m_hexconverter.getUint16Le(m_rawDataBytes, index);
		index += 2;
		int month = m_hexconverter.getUint8(m_rawDataBytes, index);
		index++;
		int day = m_hexconverter.getUint8(m_rawDataBytes, index);
		index++;
		int hour = m_hexconverter.getUint8(m_rawDataBytes, index);
		index++;
		int minute = m_hexconverter.getUint8(m_rawDataBytes, index);
		index++;
		int sec = m_hexconverter.getUint8(m_rawDataBytes, index);
		index++;

		m_timeStamp = LocalDateTime.of(year, month, day, hour, minute, sec);
		if (m_timeOffsetPresent) {
			int minutes = m_hexconverter.getSint16Le(m_rawDataBytes, index);
			index += 2;
			// <Enumeration key="32767" value="Overrun" />
			// < Enumeration key = "32768" value = "Underrun" />
			if (minutes == 32767 || minutes == 32768) {
				m_timeOffset = 0;
			} else {
				m_timeOffset = minutes;
				m_timeStamp = m_timeStamp.plusMinutes(minutes);
			}
		} else {
			m_timeOffset = 0;
		}

		if (m_hasMeasurementConcentrationTypeAndSampleLocation) {
			// Bit 1 of Flags is set to 1.
			// Next fields are Glucose Concentration, Type and Sample Location Present
			// Requires: [C2]
			// Glucose Concentration Units are either kg/L or mol/L,
			//   depending on Flags' bit 2 but both are SFLOAT (2 bytes).
			// The SFLOAT-Type is defined as a 16-bit value
			//   with 12-bit mantissa and 4-bit exponent.
			// 16-bit value with 4-bit exponent and 12-bit mantissa.
			int conc = m_hexconverter.getUint16Le(m_rawDataBytes, index);
			index += 2;
			// (int) ==> (SFLOAT):
			int mantissa = conc & 0x0FFF;
			int exponent = conc >> 12;

			if (exponent >= 0x0008) {
				exponent = -((0x000F + 1) - exponent);
			}

			if (mantissa >= 0x0800) {
				mantissa = -((0x0FFF + 1) - mantissa);
			}

			double magnitude = Math.pow(10.0f, exponent);
			double value = (mantissa * magnitude);
			if (m_unitsAre_kgPerLiter) {
				// Convert to mg/dL
				value *= 100000;
			}

			m_measurement = (int) Math.round(value);  // Math.round(double) returns a long
		}
	}
	/*
	public OneGlucoseReading(byte[] data)
		{
			int index;

			index = 0;
			RawData = new ByteData(data);

			Flags = data[index];
			index++;
			TimeOffsetPresent = (Flags & 0x01) != 0;
			HasMeasurementConcentrationTypeAndSampleLocation = (Flags & 0x02) != 0;
			UnitsAre_molPerLiter = (Flags & 0x04) != 0;
			UnitsAre_kgPerLiter = !UnitsAre_molPerLiter;
			SensorStatusAnnunciationPresent = (Flags & 0x08) != 0;
			ContextInformationFollows = (Flags & 0x10) != 0;
			//
			// 2. Sequence Number [Mandatory] (uint16)
			//    (Little Endian) Ex: 71 00 ==> 0x0071 = 113 decimal
			SequenceNumber = data[index + 1];  // (Most significant)
			SequenceNumber &= 0xff;
			SequenceNumber <<= 8;
			SequenceNumber |= data[index];  // (Least significant)
			index += 2;
			//
			// 3. Base Time [Mandatory] - org.bluetooth.characteristic.date_time
			//    (See characteristic_date_time.xml in "Docs And Notes")
			//                   +=============>
			//    0000  0b 87 00 e3  07 05 0e 04
			//    0008  05 0d 74 00  5d b0 11 00
			//         <====+, length = 7.
			Int32 year = data[index + 1];  // 0x07
			year &= 0xff;
			year <<= 8;
			year |= data[index];  // 0x07e3 == 2,019 decimal
			index += 2;
			Int32 month = data[index];
			index++;
			Int32 day = data[index];
			index++;
			Int32 hour = data[index];
			index++;
			Int32 min = data[index];
			index++;
			Int32 sec = data[index];
			index++;

			Timestamp = new DateTime(year, month, day, hour, min, sec);
			if (TimeOffsetPresent)
			{
				TimeOffset = data[index + 1];
				TimeOffset <<= 8;
				TimeOffset |= data[index];
				index += 2;
				// <Enumeration key="32767" value="Overrun" />
				// < Enumeration key = "32768" value = "Underrun" />
				if (TimeOffset == 32767 || TimeOffset == 32768)
				{
					TimeOffset = 0;
				}
				else
				{
					Timestamp = Timestamp.AddMinutes(TimeOffset);
				}
			}
			else
			{
				TimeOffset = 0;
			}
			if (HasMeasurementConcentrationTypeAndSampleLocation)
			{
				// Bit 1 of Flags is set to 1.
				// Next fields are Glucose Concentration, Type and Sample Location Present
				// Requires: [C2]
				// Glucose Concentration Units are either kg/L or mol/L,
				//   depending on Flags' bit 2 but both are SFLOAT (2 bytes).
				// The SFLOAT-Type is defined as a 16-bit value
				//   with 12-bit mantissa and 4-bit exponent.
				// 16-bit value with 4-bit exponent and 12-bit mantissa.
				int conc = data[index + 1];
				conc = conc & 0xff;
				conc <<= 8;
				conc |= data[index];
				index += 2;


				int mantissa = conc & 0x0FFF;
				int exponent = conc >> 12;

				if (exponent >= 0x0008)
				{
					exponent = -((0x000F + 1) - exponent);
				}

				if (mantissa >= 0x0800)
				{
					mantissa = -((0x0FFF + 1) - mantissa);
				}

				double magnitude = Math.Pow(10.0f, exponent);
				double value = (mantissa * magnitude);
				if (UnitsAre_kgPerLiter)
				{
					// Convert to mg/dL
					value *= 100000;
				}
				Measurement = (int)Math.Round(value);

				// Type is HO nibble of next bytes:
				// SampleType class just wants the raw data
				//   and handles the nibble >> part for us...
				Type = new SampleType(data[index]);

				// Sample Location is LO nibble:
				Location = new SampleLocation(data[index]);
				index++;

			}  // if (HasMeasurementConcentrationTypeAndSampleLocation)
			else
			{
				Type = new SampleType();  // .ToString() returns "(unspecified)"
				Location = new SampleLocation();  // .ToString() returns "(unspecified)"

			}

			if (SensorStatusAnnunciationPresent)
			{
				// Sensor Status Annunciation is present if Bit 3 of Flags is set (1)
				// Type: 16-bits
				Status = new SensorStatus(data, index);
				index += 2;
			}
			else
			{
				Status = new SensorStatus();  // .ToString() returns "(unspecified)"
			}

		}
	 */
}
