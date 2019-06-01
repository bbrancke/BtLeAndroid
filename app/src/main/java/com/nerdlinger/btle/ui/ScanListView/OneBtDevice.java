package com.nerdlinger.btle.ui.ScanListView;

public class OneBtDevice implements Comparable<OneBtDevice>{
	private String m_id;
	private String deviceName;
	private String deviceBdAddr;

	public OneBtDevice(int id, String name, String bd_addr) {
		m_id = String.format("%d", id);
		deviceName = name;
		deviceBdAddr = bd_addr;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public String getDeviceBdAddr() {
		return deviceBdAddr;
	}

	public String getId() { return  m_id; }

	public int compareTo(OneBtDevice other) {
		return getDeviceName().compareTo(other.getDeviceName());
	}
}

