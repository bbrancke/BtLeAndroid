package com.nerdlinger.btle.GlucoseReading;

import android.util.Log;

public class SampleLocation {
	private int m_location;

	public void setValueLocation(int loc) {
		m_location = loc;
	}

	public void setStringLocation(String s) {
		try {
			setValueLocation(Integer.decode(s));
		}
		catch (Exception ex) {
			Log.e("SampleLocation", "Can't parse value [" + s + "'");
		}
	}

	public int getValueLocation() {
		return m_location;
	}

	// Return (int) as s String:
	public String getStringLocationValue() {
		return String.format("%d", m_location);
	}

	private String m_toString = "";

	// Return Location as ToString(), e.g. "Finger", "Arm", etc.:
	public String getStringLocationString() {
		if (m_toString.length() < 1) {
			if (m_location >= 0 && m_location <= 15) {
				if (m_location <= 4) {
					m_toString = m_sampleLocations[m_location];
				}
				else if (m_location == 15) {
					m_toString = "Location Not Available";
				}
				else {
					m_toString = "Reserved";
				}
			}
			else
			{
				m_toString = "(unknown: " + String.format("%d", m_location) + ")";
			}
		}
		return m_toString;
	}

	public SampleLocation()
	{
		m_toString = "(unspecified)";
	}

	public SampleLocation(int value)
	{
		m_location = value;
		m_location &= 0x0f;
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
	private String[] m_sampleLocations =
	{
		"Reserved0",  // 0
		"Finger",  // 1
		"Alternate Test Site",  // 2
		"Earlobe",  // 3
		"Control Solution",  // 4,
		// NotAvailable  15
		// 5->14 "Reserved"
	};
}
