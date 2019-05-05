package com.nerdlinger.btle.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.nerdlinger.btle.Constants;

import java.util.ArrayList;
import java.util.List;

public class BleScanner {
	private BluetoothLeScanner scanner = null;
	private BluetoothAdapter bluetooth_adapter = null;
	private Handler handler = new Handler();
	private ScanResultsConsumer scan_results_consumer;
	private Context context;
	private boolean scanning=false;
	private String device_name_start="";
	private long m_scanTimeRemaining_ms;

	public BleScanner(Context context) {
		this.context = context;
		final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		bluetooth_adapter = bluetoothManager.getAdapter();
        // check bluetooth is available and on
		if (bluetooth_adapter == null || !bluetooth_adapter.isEnabled()) {
			log("Bluetooth is NOT switched on");
			Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE);
			enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(enableBtIntent);
		}
		log("Bluetooth is switched on");
	}

	private void log(String msg) {
		Log.e(Constants.TAG, "BleScanner: " + msg);
	}

	// ScanResultsConsumer is an INTERFACE, implemented by MainActivity.
	//   Currently has three methods: CandidateBleDevice, scanningStarted, scanningStopped
	public void startScanning(final ScanResultsConsumer scan_results_consumer, long stop_after_ms) {
		if (scanning) {
			log("Already scanning, ignoring startScanning request");
			return;
		}
		if (scanner == null) {
			scanner = bluetooth_adapter.getBluetoothLeScanner();
			log("Created BluetoothScanner object");
		}
		this.scan_results_consumer = scan_results_consumer;
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (scanning) {
					log("Stopping scanning");
					scanner.stopScan(scan_callback);
					setScanning(false);
				} }
		}, stop_after_ms);

		m_scanTimeRemaining_ms = stop_after_ms;
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!scanning) {
					return;
				}
				m_scanTimeRemaining_ms -= 1000;
				if (m_scanTimeRemaining_ms <= 500)
				{
					log("Time up, stopping scan.");
					scanner.stopScan(scan_callback);
					setScanning(false);
					return;
				}
				// (else...)
				scan_results_consumer.scanTimeRemaining_ms(m_scanTimeRemaining_ms);
				handler.postDelayed(this, 1000);
			}
		}, 1000);
/*****
To find only devices named "BDSK":
 List<ScanFilter> filters;
 filters = new ArrayList<ScanFilter>();
 ScanFilter filter = new ScanFilter.Builder().setDeviceName("BDSK").build();
 filters.add(filter);
 ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
 setScanning(true);
 scanner.startScan(filters, settings, scan_callback);
                   +=====+
Here I want to see everything for now...
*****/

		log("Starting Scan...");
		List<ScanFilter> filters;
		filters = new ArrayList<ScanFilter>();
		ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
		setScanning(true);
		scanner.startScan(filters, settings, scan_callback);
	}

	public void stopScanning() {
		setScanning(false);
		log("Stopping scanning");
		scanner.stopScan(scan_callback);
	}

	public boolean isScanning() {
		return scanning;
	}
	void setScanning(boolean scanning) {
		this.scanning = scanning;
		if (!scanning) {
			scan_results_consumer.scanningStopped();
		} else {
			scan_results_consumer.scanningStarted(); }
	}

	// ============================================================
	private ScanCallback scan_callback = new ScanCallback() {
		public void onScanResult(int callbackType, final ScanResult result) {
			if (!scanning) {
				return;
			}
			scan_results_consumer.candidateBleDevice(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
		}
	};
}
