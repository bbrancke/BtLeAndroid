package com.nerdlinger.btle.ui;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nerdlinger.btle.Constants;
import com.nerdlinger.btle.R;
import com.nerdlinger.btle.bluetooth.BleAdapterService;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceEventActivity01 extends Activity {
	public List<OneDeviceEvent> m_list = new ArrayList<>();

	private UuidLookup m_uuidlookup = new UuidLookup();

	private String m_deviceName;
	private String m_deviceBdaddr;

	private MsgHandler m_msgHandler;
	private List<String> m_deviceServices = new ArrayList<>();
	private List<String> m_serviceCharacteristics = new ArrayList<>();

	private boolean m_connected = false;
	private Button m_btnConnect;

	private ImageView m_imgConnected;
	private ImageView m_imgDisconnected;
	private ListView m_listView;
	private DeviceEventListViewAdapter m_eventListAdapter;

	private int m_nextEventNumber;
	private String m_events;

	private DateFormat m_dateFormat;

	private BleAdapterService m_bleAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		TextView tv;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device01);
		// read intent data
		final Intent intent = getIntent();
		m_deviceName = intent.getStringExtra("name");
		m_deviceBdaddr = intent.getStringExtra("id");

		m_msgHandler = new MsgHandler(this);

		m_nextEventNumber = 1;
		m_events = "";

		//m_dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		m_dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

		tv = findViewById(R.id.tvDeviceName);
		tv.setText(m_deviceName);

		tv = findViewById(R.id.tvBdAddr);
		tv.setText(m_deviceBdaddr);

		m_imgConnected = findViewById(R.id.imgstatusconnected);
		m_imgDisconnected = findViewById(R.id.imgstatusdisconnected);

		m_listView = findViewById(R.id.lvEvents);
		m_eventListAdapter = new DeviceEventListViewAdapter(this, m_list);
		m_listView.setAdapter(m_eventListAdapter);
		/*
		m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> l, View v, int position, long id)
			{
				// 'View v' is the row that was selected; has 3 TextView children:
				//    Id, Name and Status...
				String msg = "Item #: " + position;
				m_tvStatus.setText(msg);
				// Toast.makeText(m_appContext, "Item #: " + position, Toast.LENGTH_SHORT ).show();
			}
		});
		 */
		for (int i = 1; i <= 10; i++)
		{
			String id = String.format("%d", i);
			String event = "Event # " + id;
			AddEvent(event);
		}

		// Connect to the Bluetooth adapter service
		Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
		bindService(gattServiceIntent, service_connection, BIND_AUTO_CREATE);


		m_btnConnect = findViewById(R.id.btnConnect);
		m_btnConnect.setText(R.string.btnConnect_connectText);
		m_btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				m_connected = !m_connected;
				if (m_connected) {
					AddEvent("Connecting...");

					m_btnConnect.setText(R.string.btnConnect_disconnectText);
					m_imgConnected.setVisibility(View.VISIBLE);
					m_imgDisconnected.setVisibility(View.GONE);

					m_bleAdapter.connect(m_deviceBdaddr);
//					bluetooth_le_adapter.connect(device_address))
					// BT connect is async, we get a notification upon "Connect Complete"...
					// TODO: Disable Connect Button until we get connect success / failed...
				}
				else {
					AddEvent("Disconnecting...");
					m_btnConnect.setText(R.string.btnConnect_connectText);
					m_imgConnected.setVisibility(View.GONE);
					m_imgDisconnected.setVisibility(View.VISIBLE);
				}

			}
		});
	}

	private void log(String msg) {
		Log.e(Constants.TAG, "DeviceEventActivity01: " + msg);
	}

	void AddEvent(String msg) {
		Date date = new Date();
		String now = m_dateFormat.format(date);
		String event = now + ": " + msg;
		String id = String.format("%d", m_nextEventNumber);
		OneDeviceEvent ode = new OneDeviceEvent(id, event);
		m_list.add((ode));
		m_eventListAdapter.notifyDataSetChanged();
		m_listView.smoothScrollToPosition(m_eventListAdapter.getCount() - 1);
		m_events += event + "\n";
	}
/*
GATT Server (Device) has one or more Services:
+===============================+
| Service                       |
|   +-------------------------+ |
|   + Characteristic          + |
|   +                         + |
|   +  +-------------------+  + |
|   +  + Descriptor        +  + |
|   +  +-------------------+  + |
|   +     ... D2, D3, ...     + |
|   +-------------------------+ |
|     ... C2, C3, ...           |
+===============================+
+=========== S2, S3, ... =======+
 */
	private final ServiceConnection service_connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			m_bleAdapter = ((BleAdapterService.LocalBinder) service).getService();
			//m_bleAdapter.setActivityHandler(message_handler);

            m_bleAdapter.setActivityHandler(m_msgHandler);
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			m_bleAdapter = null;
		}
	};
    // MsgHandler class avoids this warning:
    // This Handler class should be static or leaks might occur: message_handler
    // message_hnadler holds an implicit hard ref to parent which can go out
    // of scope while message_handler is still getting msgs on the parent's
    // msg loop. Solution is to extend as a (private child) class with
    // a "Weak Ref" to parent.
    // MsgHandler replaces this:
    //    @SuppressLint("HandlerLeak")
    //    @Override public void handleMessage(Message msg) { ... }
	private static class MsgHandler extends Handler {
	    private final WeakReference<DeviceEventActivity01> m_parent;
	    public MsgHandler(DeviceEventActivity01 parent) {
	        m_parent = new WeakReference<DeviceEventActivity01>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            boolean found;
            String service_uuid = "";
            String characteristic_uuid = "";
            byte[] b = null;

	        DeviceEventActivity01 parent = m_parent.get();
	        if (parent == null) {
	            return;
            }
            switch (msg.what) {
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    //showMsg(text);
                    parent.AddEvent(text);
                    break;

                case BleAdapterService.GATT_CONNECTED:
                    parent.AddEvent("Connected, calling DiscoverServices()");

                    //
                    // https://stackoverflow.com/questions/45056566/android-ble-gatt-connection-change-statuses
                    // this sleep is here to avoid TONS of problems in BLE, that occur whenever we start
                    // service discovery immediately after the connection is established
                    // BB: Seeing Disconnected immediately aftger Services Discovered event
                    // status: 19, means "Other device Disconnected"
                    // LATER: No the device disconnects after ten seconds
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // This is async, we get SERVICES_DISCOVERED shortly...
                    parent.m_bleAdapter.discoverServices();
                    break;

                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    found = false;
                    // m_deviceServices: List<String>
                    parent.AddEvent("Services discovered:");
                    if (parent.m_bleAdapter.GetDeviceServices(parent.m_deviceServices)) {
                        for (String s : parent.m_deviceServices) {
                            if (s.equals(parent.m_uuidlookup.GlucoseServiceId)) {
                                found = true;
                            }
                            String svc_name = parent.m_uuidlookup.lookup(s);
                            parent.AddEvent("SVC: " + s + " : " + svc_name);
                        }
                    }
                    if (!found) {
                        parent.AddEvent("Glucose Service Not detected");
                    }
                    else {
                        parent.AddEvent("Getting Glucose Service characteristics...");
                        if (parent.m_bleAdapter.GetServicesCharacteristics(parent.m_uuidlookup.GlucoseServiceId,
                                parent.m_serviceCharacteristics)) {
                            for (String s : parent.m_serviceCharacteristics) {
                                String chr_name = parent.m_uuidlookup.lookup(s);
                                parent.AddEvent("CHR: " + s + " : " + chr_name);
                            }
                        }
                        else {
                            parent.AddEvent("NO CHARACTERISTICS DETECTED");
                        }
                    }

/*                    List<BluetoothGattService> slist = parent.m_bleAdapter.getSupportedGattServices();
                    boolean link_loss_present = false;
                    boolean immediate_alert_present = false;
                    boolean tx_power_present = false;
                    boolean proximity_monitoring_present = false;
                    boolean health_thermometer_present = false;
                    for (BluetoothGattService svc : slist) {
                        parent.AddEvent("SVC: UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());
                        //if (svc.getUuid().toString().equalsIgnoreCase(BleAdapterService.LINK_LOSS_SERVICE_UUID)) {
                        //	link_loss_present = true;
                        //	continue;
                        //}  .. and more tests ...
                    }
*/
                    break;

                case BleAdapterService.GATT_DISCONNECT:
                    bundle = msg.getData();
                    int reason = bundle.getInt(BleAdapterService.PARCEL_STATUS, 0);
//					((Button) PeripheralControlActivity.this
//							.findViewById(R.id.connectButton)).setEnabled(true);
                    // we're disconnected
//					showMsg("DISCONNECTED (" + String.format("%d", reason) + ")");
                    parent.AddEvent("DISCONNECTED (" + String.format("%d", reason) + ")");
//					if (back_requested) {
//						PeripheralControlActivity.this.finish();
//					}
                    break;
            }
        }
    }

}

/*
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
					List<BluetoothGattService> slist = bluetooth_le_adapter.getSupportedGattServices();
					boolean link_loss_present=false;
					boolean immediate_alert_present=false;
					boolean tx_power_present=false;
					boolean proximity_monitoring_present=false;
					boolean health_thermometer_present = false;
					for (BluetoothGattService svc : slist) {
						log("UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());
						//if (svc.getUuid().toString().equalsIgnoreCase(BleAdapterService.LINK_LOSS_SERVICE_UUID)) {
						//	link_loss_present = true;
						//	continue;
						//}  .. and more tests ...
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


 */