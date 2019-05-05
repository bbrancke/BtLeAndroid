package com.nerdlinger.btle.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface ScanResultsConsumer {
	public void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi);
	public void scanningStarted();
	public void scanTimeRemaining_ms(long ms);
	public void scanningStopped();
}
