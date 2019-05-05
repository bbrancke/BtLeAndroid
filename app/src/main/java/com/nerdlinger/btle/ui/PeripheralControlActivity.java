package com.nerdlinger.btle.ui;

import android.app.Activity;
import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
//import com.bluetooth.bdsk.bluetooth.BleAdapterService;
import com.nerdlinger.btle.Constants;
import com.nerdlinger.btle.R;
import com.nerdlinger.btle.bluetooth.BleAdapterService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class PeripheralControlActivity extends Activity {
	public static final String EXTRA_NAME = "name";
	public static final String EXTRA_ID = "id";

	private BleAdapterService bluetooth_le_adapter;

	private String device_name;
	private String device_address;
	private Timer mTimer;
	private boolean sound_alarm_on_disconnect = false;
	private int alert_level;
	private boolean back_requested = false;
	private boolean share_with_server = false;
	private Switch share_switch;
	private Button m_connectButton;

	private List<String> m_deviceServices = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peripheral_control);
		// read intent data
		final Intent intent = getIntent();
		device_name = intent.getStringExtra(EXTRA_NAME);
		device_address = intent.getStringExtra(EXTRA_ID);
		m_connectButton = findViewById(R.id.connectButton);
		// show the device name
		((TextView) this.findViewById(R.id.nameTextView)).setText("Device : " + device_name + " [" + device_address + "]");
		// disable the noise button
		((Button) PeripheralControlActivity.this.findViewById(R.id.noiseButton))
				.setEnabled(false);
		// disable the LOW/MID/HIGH alert level selection buttons
		((Button) this.findViewById(R.id.lowButton)).setEnabled(false);
		((Button) this.findViewById(R.id.midButton)).setEnabled(false);
		((Button) this.findViewById(R.id.highButton)).setEnabled(false);
		share_switch = (Switch) this.findViewById(R.id.switch1);
		share_switch.setEnabled(false);
		share_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
			                             boolean isChecked) {
				// we'll complete this later
			}
		});
		// Connect to the Bluetooth adapter service
		Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
		bindService(gattServiceIntent, service_connection, BIND_AUTO_CREATE);
		showMsg("READY");

	}

	private void log(String msg) {
		Log.e(Constants.TAG, "PeriphCtlActivity: " + msg);
	}

	public void onBackPressed() {
		Log.d(Constants.TAG, "onBackPressed");
		back_requested = true;
		if (bluetooth_le_adapter.isConnected()) {
			// We will call finish() when we get DisconnectComplete event.
			try {
				bluetooth_le_adapter.disconnect();
			} catch (Exception e) {
			}
		} else {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(service_connection);
		bluetooth_le_adapter = null;
	}

	private void showMsg(final String msg) {
		log(msg);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((TextView) findViewById(R.id.msgTextView)).setText(msg);
			}
		});
	}

	private final ServiceConnection service_connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
			bluetooth_le_adapter.setActivityHandler(message_handler);
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			bluetooth_le_adapter = null;
		}
	};

	//@SuppressLint("HandlerLeak")
	private Handler message_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle;
			String service_uuid = "";
			String characteristic_uuid = "";
			byte[] b = null;
			// message handling logic
			switch (msg.what) {
				case BleAdapterService.MESSAGE:
					bundle = msg.getData();
					String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
					showMsg(text);
					break;

				case BleAdapterService.GATT_CONNECTED:
					((Button) PeripheralControlActivity.this
							.findViewById(R.id.connectButton)).setEnabled(false);
					// we're connected
					showMsg("CONNECTED");
					//
					// https://stackoverflow.com/questions/45056566/android-ble-gatt-connection-change-statuses
					// this sleep is here to avoid TONS of problems in BLE, that occur whenever we start
					// service discovery immediately after the connection is established
					// BB: Seeing Disconnected immediately aftger Services Discovered event
					// status: 19, means "Other device Disconnected
					try {
						Thread.sleep(600);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					bluetooth_le_adapter.discoverServices();
					break;
				case BleAdapterService.GATT_DISCONNECT:
					bundle = msg.getData();
					int reason = bundle.getInt(BleAdapterService.PARCEL_STATUS, 0);
					((Button) PeripheralControlActivity.this
							.findViewById(R.id.connectButton)).setEnabled(true);
					// we're disconnected
					showMsg("DISCONNECTED (" + String.format("%d", reason) + ")");
					if (back_requested) {
						PeripheralControlActivity.this.finish();
					}
					break;

				case BleAdapterService.GATT_SERVICES_DISCOVERED:
					// validate services and if ok....
					if (bluetooth_le_adapter.GetDeviceServices(m_deviceServices)) {
						for (String s : m_deviceServices) {
							log("Service: UUID=[" + s + "]");
						}
					}
					else {
						log("Can't get Device Services");
					}

					break;
			}
		}
	};

	// Button click handlers (defined in activity_peripheral_control.xml):
	public void onLow(View view) {
	}

	public void onMid(View view) {
	}

	public void onHigh(View view) {
	}

	public void onNoise(View view) {
	}

	public void onConnect(View view) {
		log("onConnect clicked");
		if (bluetooth_le_adapter == null) {
			showMsg("Can't connect, BT adapter is null");
			return;
		}
		// (else...)
		if (bluetooth_le_adapter.connect(device_address)) {
			// BT connect is async, we get a notification upon "Connect Complete"...
			// For now, disable the Connect button:
			m_connectButton.setEnabled(false);
			return;
		}

		// (else...)
		showMsg("onConnect(): FAILED.");
	}


}

