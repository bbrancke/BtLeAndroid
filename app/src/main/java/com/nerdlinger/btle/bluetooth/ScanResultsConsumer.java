package com.nerdlinger.btle.bluetooth;

import android.bluetooth.BluetoothDevice;

// MainActivity extends AppCompatActivity implements ScanResultsConsumer.
//   These methods are implemented in MainActivity.
// BleScanner has a "ScanCallback" which calls "candidateBleDevice"
//   and the other methods.
public interface ScanResultsConsumer {
	public void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi);
	public void scanningStarted();
	public void scanTimeRemaining_ms(long ms);
	public void scanningStopped();
}
