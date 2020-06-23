package com.nerdlinger.btle.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nerdlinger.btle.Constants;
import com.nerdlinger.btle.GlucoseReading.OneReading;
import com.nerdlinger.btle.R;
import com.nerdlinger.btle.bluetooth.BleScanner;
import com.nerdlinger.btle.bluetooth.ScanResultsConsumer;
import com.nerdlinger.btle.ui.ScanListView.DeviceTouchListener;
import com.nerdlinger.btle.ui.ScanListView.OneBtDevice;
import com.nerdlinger.btle.ui.ScanListView.ScanRecyclerViewAdapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ScanResultsConsumer {
	private boolean m_bleNowScanning = false;
	private Handler handler = new Handler();
	private RecyclerView m_rvScanResults;
	private RecyclerView.Adapter m_rvAdapter;
	private RecyclerView.LayoutManager m_rvLayoutManager;
	private Button m_scanButton;
	private BleScanner m_bleScanner;
	private static final long SCAN_TIMEOUT = 30000;  // 5000;
	private static final int REQUEST_LOCATION = 0;
	//private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
	private boolean m_permissionsLocationGranted = false;

	private static final int REQUEST_STORAGE_WRITE = 1;
	private boolean m_permissionsStorageGranted = false;

	private int mdeviceCount = 0;
	private Toast toast;

	private List<OneBtDevice> m_devices = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		// 07:09:40.481: CHR INDICATION/NOTIFICATON RECEIVED.
		//07:09:40.485: 00002a18-0000-1000-8000-00805f9b34fb:    Data Received, length = 17
		//0000  0b 07 00 e2  07 03 08 04
		//0008  18 39 3c 00  76 b0 11 00
		//0010  00

		byte[] data = new byte[17];
		data[0] = 0x0b; data[1] = 0x07; data[2] = 0x00;
		//data[3] = 0xe2 & 0xff;
		data[3] = 0x72; data[3] += 0x70;
		data[4] = 0x07; data[5] = 0x03;
		data[6] = 0x08; data[7] = 0x04;
		data[8] = 0x18; data[9] = 0x39; data[10] = 0x3c; data[11] = 0x00;
		data[12] = 0x76; data[13] = (byte)0xb0; data[14] = 0x11; data[15] = 0x00;
		data[16] = 0x00;

		OneReading reading = new OneReading();
		reading.SetRawData(1, data);
		reading.Parse();
		int n = reading.GetSequenceNumber();
		int m = reading.GetMeasurement();
		String when = reading.GetTimeStamp();
		String s = "[" + when + "]: " + String.format("%d", m);

		Log.e("BRAD", s);



		m_scanButton = findViewById(R.id.scanButton);

		m_rvScanResults = findViewById(R.id.scan_results_recycler_view);
		// This setting improves performance. Content changes
		// will not change the layout size:
		m_rvScanResults.setHasFixedSize(true);
		// Use a linear layout manager:
		m_rvLayoutManager = new LinearLayoutManager(this);
		m_rvScanResults.setLayoutManager(m_rvLayoutManager);
		//m_rvScanResults.setItemAnimator(new DefaultItemAnimator());  // Default *IS* already a DefaultItemAnimator!

		// Specify an adapter:
		m_rvAdapter = new ScanRecyclerViewAdapter(m_devices);
		// Add a divider line between each Device:
		Drawable row_divider = ContextCompat.getDrawable(this, R.drawable.recycler_view_divider);
		RecyclerViewRowDivider rowDivider = new RecyclerViewRowDivider(row_divider);
		m_rvScanResults.addItemDecoration(rowDivider);


		m_rvScanResults.setAdapter(m_rvAdapter);

		m_rvScanResults.addOnItemTouchListener(new DeviceTouchListener(getApplicationContext(),
				m_rvScanResults, new DeviceTouchListener.ClickListener() {
			@Override
			public void onClick(View view, int position) {
				if (m_bleNowScanning) {
					setScanState(false);
					m_bleScanner.stopScanning();
				}
				if (!m_permissionsStorageGranted) {
					return;
				}
				OneBtDevice device = m_devices.get(position);
// Proves that we are getting the correct device (yes, we are):
//				Toast.makeText(getApplicationContext(),
//						"Clicked: " + device.getDeviceBdAddr(),
//						Toast.LENGTH_SHORT).show();

				// Fire off PeripheralControlActivity:

				//Intent intent = new Intent(MainActivity.this,
				//		PeripheralControlActivity.class);

				Intent intent = new Intent(MainActivity.this, DeviceEventActivity01.class);
				intent.putExtra("name", device.getDeviceName());
				intent.putExtra("id", device.getDeviceBdAddr());
				startActivity(intent);


			}

			@Override
			public void onLongClick(View view, int position) {

			}
		}));
		setButtonText();
		m_bleScanner = new BleScanner(this.getApplicationContext());
		VerifyStoragePermissions();
	}  // onCreate()

	private void StopScan() {
		setScanState(false);
	}

	/******************************************
	 * AndroidManifest:
	 * manifest xmlns:android="http://schemas.android.com/apk/res/android"
	 *           package="com.nerdlinger.btle">
	 *
	 *     <uses-permission android:name="android.permission.BLUETOOTH" />
	 *     <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
	 *     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
	 *
	 *     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	 *
	 *     <application   .....
	 ******************************************/



	private boolean VerifyStoragePermissions() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			m_permissionsStorageGranted = true;
			return true;
		}
		// Marshmallow and above: App must request permissions
		// at runtime (AND in the manifest.xml).
		// Else User can go Settings->Apps->(this app)->Permissions
		m_permissionsStorageGranted =
				checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
						==
						PackageManager.PERMISSION_GRANTED;
		if (!m_permissionsStorageGranted) {
			// .shouldShowRequestPermissionRationale() ? - Alert dlg: Reasons...
			// We get (locally defined constant) REQUEST_STORAGE_WRITE
			// in the "onRequestPermissionsResult()" method below.
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_STORAGE_WRITE);
			// I am not sure if requestPermissions() causes this to block
			// until User accepts or rejects, then onReqestPermission callback
			// is complete or if we return right here...
			// We check m_permissionsStorage on moving to the next Device Event List Activity
		}
		return m_permissionsStorageGranted;
	}


	private void requestLocationPermission() {
		Log.e(Constants.TAG, "Requesting Location permission...");
		if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.ACCESS_COARSE_LOCATION)) {
			Log.e(Constants.TAG, "Displaying location perm rationale...");
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Permission Required");
			builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					Log.e(Constants.TAG, "Requesting perms after explanation");
					ActivityCompat.requestPermissions(MainActivity.this,
							new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
							REQUEST_LOCATION);
				}
			});
			builder.show();
		} else {
			ActivityCompat.requestPermissions(this,
					new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_LOCATION);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_STORAGE_WRITE) {
			for (int i : grantResults) {
				if (i == PackageManager.PERMISSION_GRANTED) {
					// This is retarded, should just have a single call()
					// that checks version >= .M and then OS call checkSelfPerms()
					// instead of these bools...
					m_permissionsStorageGranted = true;
				}
			}
			return;
		}

		if (requestCode != REQUEST_LOCATION) {

			return;
		}
		// (else...)
		if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Log.e(Constants.TAG, "Permissions CB: Location permission has now been granted. Scanning.....");
			m_permissionsLocationGranted = true;
			if (!m_bleScanner.isScanning()) {  // NOTE: WAS: "if (ble_scanner.isScanning()) { start.. } "
				startScanning();
			}
			return;
		}
		Log.e(Constants.TAG, "Permissions CB: Location position NOT granted!");

	}


	public void onFindConnectedDevices(View view) {
		// TODO Test if already doing stuff, verify permissions.
		mdeviceCount = 0;
		m_devices.clear();

		// Fill m_devices from the returned values.
		m_bleScanner.GetConnectedDevices(m_devices);
		m_rvAdapter.notifyDataSetChanged();
	}

	public void onScan(View view) {
		if (m_bleScanner.isScanning()) {
			m_bleScanner.stopScanning();
			return;
		}
		// (else...)
		mdeviceCount = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Marshmallow and above: App must request permissions
			// at runtime (AND in the manifest.xml).
			// Else User can go Settings->Apps->(this app)->Permissions
			if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {
				m_permissionsLocationGranted = false;
				requestLocationPermission();
			}
			else {
				Log.e(Constants.TAG, "Location permission already granted, starting scan...");
				m_permissionsLocationGranted = true;
			}
		}
		else {
			// Older than 'M', didn't require run-time permission check.
			m_permissionsLocationGranted = true;
		}

		startScanning();
	}

	private void startScanning() {
		if (!m_permissionsLocationGranted)
		{
			Log.e(Constants.TAG, "startScanning(): Permission not granted.");
			return;
		}
		// (else...)
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// m_rvAdapter.clear();
				m_devices.clear();
				m_rvAdapter.notifyDataSetChanged();
			}
		});

		setScanState(true);
		m_bleScanner.startScanning(this, SCAN_TIMEOUT);

	}

	private void setButtonText() {
		final String btn_text;
		btn_text = m_bleNowScanning ? Constants.STOP_SCANNING : Constants.START_SCANNING;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				m_scanButton.setText(btn_text);
			}
		});
	}

	private void setScanState(boolean value) {
		m_bleNowScanning = value;
		setButtonText();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void log(String msg) {
		Log.e(Constants.TAG, "MainActivity: " + msg);
	}

	private OneBtDevice FindDevice(String bd_addr)
	{
		for (OneBtDevice dev : m_devices) {
			if (bd_addr.compareTo(dev.getDeviceBdAddr()) == 0) {
				return dev;
			}
		}
		return null;
	}

	private void setTitleText(String msg) {
		final String titleText = msg;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				m_scanButton.setText(titleText);
			}
		});
	}
	// ==============================================================
	// candidateBleDevice(), scanningStarted(), scanTimeRemaining()
	// and scanningStopped() are here b/cos I implement the
	// "ScanResultsConsumer" *INTERFACE*,
	// and are called by 'BleScanner' when it has something interesting
	// to show. THESE ARE CALLED ON THE BleScanner THREAD
	// (it is NOT a service).
	@Override
	public void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi) {
		String addr;
		String name;
		addr = device.getAddress();
		if (addr == null) {
			addr = "(null address)";
		}
		name = device.getName();
		if (name == null) {
			name = "(null name)";
		}
		log("CandidateBleDevice() called." + device.getAddress() + "," + device.getName());

		OneBtDevice dev = FindDevice(addr);
		if (dev != null) {
			// Already in the m_devices list, skip FOR NOW.
			// Later: Update RSSI?
			return;
		}
		int i = m_devices.size() + 1;
		m_devices.add(new OneBtDevice(i, name, addr));
		m_rvAdapter.notifyDataSetChanged();

	}

	@Override
	public void scanningStarted() {
		setTitleText("Scanning...");
		log("scanningStarted() called.");
	}

	@Override
	public void scanTimeRemaining_ms(long ms) {
		long secs = ms / 1000;
		String remaining = String.format("%d", secs);
		String titleText = "Scanning: " + remaining;
		setTitleText(titleText);
		log("scanTimeRemaining: " + remaining);
	}
	@Override
	public void scanningStopped() {
		log("scanningStopped() called.");
		setTitleText("Scanning stopped");
		setScanState(false);
	}
}
