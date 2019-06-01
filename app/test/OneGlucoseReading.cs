using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ParseGlucoseData
{
	// For more into on Notes, see glucose_measurement.xml
	public class OneGlucoseReading
	{
		public Int32 SequenceNumber { get; set; }
		public DateTime Timestamp { get; set; }
		// This is the (normalized) "Readable Measurement", e.g. Display as "97":
		public int Measurement { get; set; }

		public ByteData RawData { get; set; }
		public int Flags { get; set; }
		
		public int TimeOffset { get; set; }

		public bool TimeOffsetPresent { get; set; }
		public bool HasMeasurementConcentrationTypeAndSampleLocation { get; set; }
		public bool UnitsAre_kgPerLiter { get; set; }
		public bool UnitsAre_molPerLiter { get; set; }
		public bool SensorStatusAnnunciationPresent { get; set; }
		public bool ContextInformationFollows { get; set; }
		public SampleType Type { get; set; }  // From [C2]: { Capillary/Venous } / {Whole Blood / Plasma } enums
		public SampleLocation Location { get; set; } // From [C2]: { Finger, Earlobe, Alt Site, Test Solution, ... } enum
		public SensorStatus Status { get; set; }

		// float RawMeasurement { get; set; }
		//                                                -5
		// For kg/Liter, Raw Measurement is (e.g. 9.7 x 10

		public OneGlucoseReading(byte[] data)
		{
			int index;

			index = 0;
			RawData = new ByteData(data);
			// 1. Flags: (8 bit) [Mandatory]
			//    Define which data fields are present in the Characteristic value
			//    Bit #:
			//      0: Time Offset Present (1 = yes)
			//         If 1, then field [C1] follows the time ("requires="C1")
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


	}

	// ========================================================
	public class ByteData
	{
		public byte[] RawData;
		private string m_toString = "";

		public ByteData()
		{
			m_toString = "(unspecified)";
		}

		public ByteData(byte[] value)
		{
			RawData = value;
		}

		// DataGridView tries to display (raw) byte[]
		// as an image and throws an Exception!
		public override string ToString()
		{
			if (m_toString.Length < 1)
			{
				foreach (byte b in RawData)
				{
					m_toString += b.ToString("x2") + " ";
				}
			}
			return m_toString;
		}
	}
	
	// ========================================================

	public class SampleType
	{
		public Int32 FluidType { get; set; }
		private string m_toString = "";
		public SampleType()
		{
			m_toString = "(unspecified)";
		}

		public SampleType(Int32 value)
		{
			FluidType = value;
			FluidType >>= 4;
			FluidType &= 0xff;
		}
		/*
		 * <Enumeration key="0" value="Reserved for future use" />
						<Enumeration key="1" value="Capillary Whole blood" />
						<Enumeration key="2" value="Capillary Plasma" />
						<Enumeration key="3" value="Venous Whole blood" />
						<Enumeration key="4" value="Venous Plasma" />
						<Enumeration key="5" value="Arterial Whole blood" />
						<Enumeration key="6" value="Arterial Plasma" />
						<Enumeration key="7" value="Undetermined Whole blood" />
						<Enumeration key="8" value="Undetermined Plasma" />
						<Enumeration key="9" value="Interstitial Fluid (ISF)" />
						<Enumeration key="10" value="Control Solution" />
						<ReservedForFutureUse start="11" end="15" />
		 */
		public enum SampleTypes
		{
			Reserved0 = 0,
			CapillaryWholeBlood,
			CapillaryPlasma,
			VenousWholeBlood,
			VenousPlasma,
			ArterialWholeBlood,
			ArterialPlasma,
			UndeterminedWholeBlood,
			UndeterminedPlasma,
			InterstitialFluid,
			ControlSolution,
			Reserved11,
			Reserved12,
			Reserved13,
			Reserved14,
			Reserved15
		};

		public override string ToString()
		{
			if (m_toString.Length < 1)
			{
				if (FluidType >= 0 && FluidType <= 15)
				{
					SampleTypes t = (SampleTypes)FluidType;
					m_toString = t.ToString();
				}
				else
				{
					m_toString = "(unknown: " + FluidType.ToString() + ")";
				}
			}
			return m_toString;
		}
	}

	// ========================================================

	public class SampleLocation
	{
		public Int32 Location { get; set; }
		private string m_toString = "";

		public SampleLocation()
		{
			m_toString = "(unspecified)";
		}

		public SampleLocation(Int32 value)
		{
			Location = value;
			Location &= 0x0f;
		}

		/*
		 * <Enumeration key="0" value="Reserved for future use" />
						<Enumeration key="1" value="Finger" />
						<Enumeration key="2" value="Alternate Site Test (AST)" />
						<Enumeration key="3" value="Earlobe" />
						<Enumeration key="4" value="Control solution" />
						<Enumeration key="15" value="Sample Location value not available" />
						<ReservedForFutureUse start="5" end="14" />
		 */
		public enum SampleLocations
		{
			Reserved0 = 0,
			Finger,
			AlternateTestSite,
			Earlobe,
			ControlSolution = 15,
			NotAvailable
		};

		public override string ToString()
		{
			if (m_toString.Length < 1)
			{
				if (Location >= 0 && Location <= 15)
				{
					if (Location <= 4 || Location == 15)
					{
						SampleLocations sl = (SampleLocations)Location;
						m_toString = sl.ToString();
					}
					else
					{
						m_toString = "Reserved";
					}
				}
				else
				{
					m_toString = "(unknown: " + Location.ToString() + ")";
				}
			}
			return m_toString;
		}
	}

	// ========================================================

	public class SensorStatus
	{
		// [C5]: Field exists if the key of bit 3 of the Flags field is set to 1
		// Type 16-bit flag fields
		public Int32 StatusFlags;
		private string m_toString = "";

		public SensorStatus()
		{
			m_toString = "(unspecified)";
		}

		public SensorStatus(byte[] value, int index)
		{
			try
			{
				StatusFlags = value[index + 1];
				StatusFlags <<= 8;
				StatusFlags &= 0x0000ff00;
				StatusFlags |= value[index];
			}
			catch (Exception)
			{
				StatusFlags = 0x01000000;
			}
		}

		private void _addToString(string val)
		{
			if (m_toString.Length > 0)
			{
				m_toString += "\n";
			}
			m_toString += val;
		}

		/*
		 * Bit #:	&0xNN:	Meaning:
		 *    0		0x0001	Battery Low  (at time of reading)
		 *    1		0x0002	Sensor malfunction (at time of reading)
		 *    2		0x0004	Insufficient Sample Size
		 *    3		0x0008	Strip Insertion Error
		 *    4		0x0010	Incorrect Strip Type
		 *    5		0x0020	Result too high!
		 *    6		0x0040	Result too low!
		 *    7		0x0080	Temperature too high
		 *    8		0x0100	Temperature too low
		 *    9		0x0200	Read interrupted
		 *   10		0x0400	General Device Fault
		 *   11		0x0800	Time Fault; Inaccurate Time
		 *   12-15	0x1xxx	Reserved
		 */
		public override string ToString()
		{
			if (m_toString.Length < 1)
			{
				if (StatusFlags == 0)
				{
					m_toString = "OK";
				}
				else if (StatusFlags == 0x01000000)
				{
					m_toString = "Parse Error";
				}
				else
				{
					if ((StatusFlags & 0x0001) != 0)
					{
						_addToString("Battery Low");
					}
					if ((StatusFlags & 0x0002) != 0)
					{
						_addToString("Sensor malfunction");
					}
					if ((StatusFlags & 0x0004) != 0)
					{
						_addToString("Insufficient Sample Size");
					}
					if ((StatusFlags & 0x0008) != 0)
					{
						_addToString("Strip Insertion Error");
					}
					if ((StatusFlags & 0x0010) != 0)
					{
						_addToString("Incorrect Strip Type");
					}
					if ((StatusFlags & 0x0020) != 0)
					{
						_addToString("Result too high!");
					}
					if ((StatusFlags & 0x0040) != 0)
					{
						_addToString("Result too low!");
					}
					if ((StatusFlags & 0x0080) != 0)
					{
						_addToString("Temperature too high");
					}
					if ((StatusFlags & 0x0100) != 0)
					{
						_addToString("Temperature too low");
					}
					if ((StatusFlags & 0x0200) != 0)
					{
						_addToString("Read interrupted");
					}
					if ((StatusFlags & 0x0400) != 0)
					{
						_addToString("General Device Fault");
					}
					if ((StatusFlags & 0x0800) != 0)
					{
						_addToString("Time Fault; Inaccurate Time");
					}
					if ((StatusFlags & 0x1000) != 0)
					{
						_addToString("('Reserved' Status bits are set)");
					}
					if (m_toString.Length < 1)
					{
						m_toString = "Parse Error";
					}
				}
			}
			return m_toString;
		}
	}
}
