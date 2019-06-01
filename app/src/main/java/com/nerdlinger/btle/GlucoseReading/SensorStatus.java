package com.nerdlinger.btle.GlucoseReading;

import android.util.Log;

public class SensorStatus {
	// [C5]: Field exists if the key of bit 3 of the Flags field is set to 1
	// Type 16-bit flag fields
	private int m_statusFlags;

	public int getValueStatusFlagsValue() {
		return m_statusFlags;
	}

	// For writing to SQL lite (all fields in SQLite are really varchar fields...)
	public String getStringStatusFlagsValue() {
		return String.format("%d", m_statusFlags);
	}

	private String m_toString = "";
	private void _addToString(String val)
	{
		if (m_toString.length() > 0)
		{
			m_toString += "\n";
		}
		m_toString += val;
	}
	// For displaying:
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
	public String getStringStatusFlagsString() {
		if (m_toString.length() < 1)
		{
			if (m_statusFlags == 0)
			{
				m_toString = "OK";
			}
			else if (m_statusFlags == 0x01000000)
			{
				m_toString = "Parse Error";
			}
			else
			{
				if ((m_statusFlags & 0x0001) != 0)
				{
					_addToString("Battery Low");
				}
				if ((m_statusFlags & 0x0002) != 0)
				{
					_addToString("Sensor malfunction");
				}
				if ((m_statusFlags & 0x0004) != 0)
				{
					_addToString("Insufficient Sample Size");
				}
				if ((m_statusFlags & 0x0008) != 0)
				{
					_addToString("Strip Insertion Error");
				}
				if ((m_statusFlags & 0x0010) != 0)
				{
					_addToString("Incorrect Strip Type");
				}
				if ((m_statusFlags & 0x0020) != 0)
				{
					_addToString("Result too high!");
				}
				if ((m_statusFlags & 0x0040) != 0)
				{
					_addToString("Result too low!");
				}
				if ((m_statusFlags & 0x0080) != 0)
				{
					_addToString("Temperature too high");
				}
				if ((m_statusFlags & 0x0100) != 0)
				{
					_addToString("Temperature too low");
				}
				if ((m_statusFlags & 0x0200) != 0)
				{
					_addToString("Read interrupted");
				}
				if ((m_statusFlags & 0x0400) != 0)
				{
					_addToString("General Device Fault");
				}
				if ((m_statusFlags & 0x0800) != 0)
				{
					_addToString("Time Fault; Inaccurate Time");
				}
				if ((m_statusFlags & 0x1000) != 0)
				{
					_addToString("('Reserved' Status bits are set)");
				}
				if (m_toString.length() < 1)
				{
					m_toString = "Parse Error";
				}
			}
		}
		return m_toString;
	}

	// ctor():
	public SensorStatus(byte[] value, int index)
	{
		int val;
		try
		{
			val = value[index + 1];
			val &= 0xff;
			m_statusFlags = val;
			m_statusFlags <<= 8;
			val = value[index];
			val &= 0xff;
			m_statusFlags |= val;
		}
		catch (Exception ex)
		{
			Log.e("SensorStatus", "Parse failed, index: " + String.format("%d",index) +
					": " + ex.getMessage());
			m_statusFlags = 0x01000000;
		}
	}
}
