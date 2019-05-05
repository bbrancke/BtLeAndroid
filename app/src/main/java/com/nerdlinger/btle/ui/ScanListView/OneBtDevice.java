package com.nerdlinger.btle.ui.ScanListView;

public class OneBtDevice implements Comparable<OneBtDevice>{
	private String deviceName;
	private String deviceBdAddr;

	public OneBtDevice(String name, String bd_addr) {
		deviceName = name;
		deviceBdAddr = bd_addr;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public String getDeviceBdAddr() {
		return deviceBdAddr;
	}
	public int compareTo(OneBtDevice other) {
		return getDeviceName().compareTo(other.getDeviceName());
	}
}

