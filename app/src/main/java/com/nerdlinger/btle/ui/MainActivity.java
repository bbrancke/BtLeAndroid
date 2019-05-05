package com.nerdlinger.btle.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.nerdlinger.btle.R;
import com.nerdlinger.btle.bluetooth.BleScanner;
import com.nerdlinger.btle.bluetooth.ScanResultsConsumer;
import com.nerdlinger.btle.ui.ScanListView.DeviceTouchListener;
import com.nerdlinger.btle.ui.ScanListView.OneBtDevice;
import com.nerdlinger.btle.ui.ScanListView.ScanRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ScanResultsConsumer {
	private boolean m_bleNowScanning = false;
	private Handler handler = new Handler();
	private RecyclerView m_rvScanResults;
	private RecyclerView.Adapter m_rvAdapter;
	private RecyclerView.LayoutManager m_rvLayoutManager;
	private Button m_scanButton;
	private TextView m_scanTitle;
	private BleScanner m_bleScanner;
	private static final long SCAN_TIMEOUT = 30000;  // 5000;
	private static final int REQUEST_LOCATION = 0;
	private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
	private boolean m_permissionsGranted =false;
	private int mdeviceCount =0;
	private Toast toast;

	private List<OneBtDevice> m_devices = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_scanButton = findViewById(R.id.scanButton);
		m_scanTitle = findViewById(R.id.scanTitle);

		m_rvScanResults = findViewById(R.id.scan_results_recycler_view);
		// This setting improves performance. Content changes
		// will not change the layout size:
		m_rvScanResults.setHasFixedSize(true);
		// Use a linear layout manager:
		m_rvLayoutManager = new LinearLayoutManager(this);
		m_rvScanResults.setLayoutManager(m_rvLayoutManager);
		m_rvScanResults.setItemAnimator(new DefaultItemAnimator());

		// Specify an adapter:
		m_rvAdapter = new ScanRecyclerViewAdapter(m_devices);

		// Add a divider line between each Device:
		// (This:
		//   https://www.androidhive.info/2016/01/android-working-with-recycler-view/
		// shows a fancier divider...)
		m_rvScanResults.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

		m_rvScanResults.setAdapter(m_rvAdapter);

		m_rvScanResults.addOnItemTouchListener(new DeviceTouchListener(getApplicationContext(),
				m_rvScanResults, new DeviceTouchListener.ClickListener() {
			@Override
			public void onClick(View view, int position) {
				if (m_bleNowScanning) {
					setScanState(false);
					m_bleScanner.stopScanning();
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
	}  // onCreate()

	private void StopScan() {
		setScanState(false);
	}
	/**** This is AFU! ***
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {
				if (m_bleNowScanning) {
					setScanState(false);
					super.onCreate(savedInstanceState);
					setContentView(R.layout.activity_main);
					setButtonText();
					ble_device_list_adapter = new ListAdapter();
					ListView listView = findViewById(R.id.deviceList);
					listView.setAdapter(ble_device_list_adapter);
					m_bleScanner = new BleScanner(getApplicationContext());
					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view,
						                        int position, long id) {
							if (m_bleNowScanning) {
								setScanState(false);
								30
								m_bleScanner.stopScanning();
							}
							BluetoothDevice device = ble_device_list_adapter.getDevice(position);
							if (toast != null) {
								toast.cancel();
							}
							Intent intent = new Intent(MainActivity.this,
									PeripheralControlActivity.class);
							intent.putExtra(PeripheralControlActivity.EXTRA_NAME, device.getName());
							intent.putExtra(PeripheralControlActivity.EXTRA_ID, device.getAddress());
							startActivity(intent);
						}
					});
				}
			}

		}
	}
	 ****/

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
		if (requestCode != REQUEST_LOCATION) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			return;
		}
		// (else...)
		if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Log.e(Constants.TAG, "Permissions CB: Location permission has now been granted. Scanning.....");
			m_permissionsGranted = true;
			if (!m_bleScanner.isScanning()) {  // NOTE: WAS: "if (ble_scanner.isScanning()) { start.. } "
				startScanning();
			}
			return;
		}
		Log.e(Constants.TAG, "Permissions CB: Location position NOT granted!");

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
				m_permissionsGranted = false;
				requestLocationPermission();
			}
			else {
				Log.e(Constants.TAG, "Location permission already granted, starting scan...");
				m_permissionsGranted = true;
			}
		}
		else {
			// Older than 'M', didn't require run-time permission check.
			m_permissionsGranted = true;
		}

		startScanning();
	}

	private void startScanning() {
		if (!m_permissionsGranted)
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
	// "ScanResultsCinsumer" *INTERFACE*,
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
		m_devices.add(new OneBtDevice(name, addr));
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
