package com.nerdlinger.btle.GlucoseReading;

public class SampleType {

	private int m_fluidType;
	public String getStringFluidTypeValue() {
		return String.format("%d", m_fluidType);
	}

	private String m_toString = "";
	public SampleType()
	{
		m_toString = "(unspecified)";
	}

	public SampleType(int value)
	{
		m_fluidType = value;
		m_fluidType >>= 4;
		m_fluidType &= 0xff;
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
	private String[] m_sampleTypes = {
		"Reserved0",  // 0
		"Capillary Whole Blood",  // 1
		"Capillary Plasma",  // 2
		"Venous Whole Blood",  // 3
		"Venous Plasma",  // 4
		"Arterial Whole Blood",  // 5
		"ArterialPlasma",  // 6
		"Undetermined Whole Blood",  // 7
		"Undetermined Plasma",  // 8
		"Interstitial Fluid,",  // 9
		"ControlSolution",  // 10
		"Reserved11",  // 11
		"Reserved12",  // 12
		"Reserved13",  // 13
		"Reserved14",  // 14
		"Reserved15"   // 15
	};

	public String getStringFluidTypeString() {
		if (m_toString.length() < 1) {
			if (m_fluidType >= 0 && m_fluidType <= 15) {
				m_toString = m_sampleTypes[m_fluidType];
			}
			else {
				m_toString = "(unknown: " + String.format("%d", m_fluidType) + ")";
			}
		}
		return m_toString;
	}
}
