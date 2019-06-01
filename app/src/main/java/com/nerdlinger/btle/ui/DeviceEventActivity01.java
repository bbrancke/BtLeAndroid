package com.nerdlinger.btle.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.nerdlinger.btle.bluetooth.BleAdapterService.BundleUuid;
import static com.nerdlinger.btle.bluetooth.BleAdapterService.GATT_NOTIFICATION_OR_INDICATION_RECEIVED;

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

	private Date m_startTime = new Date();

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

		AddEvent("DeviceEventActivity01: Device Name: " + m_deviceName);
		AddEvent("       Address: " + m_deviceBdaddr);

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
					m_startTime = new Date();

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
					WriteEventsToSdCard();
				}

			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(service_connection);
		m_bleAdapter.disconnect();
		m_bleAdapter = null;
	}

	private void WriteEventsToSdCard() {
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/btgludata/");
		if (!dir.exists()) {
			try {
				Files.createDirectories(dir.toPath());
			}
			catch (Exception ex) {
				String reason = ex.getMessage();
				AddEvent("CAN'T CREATE SD CARD FOLDER:");
				AddEvent(dir.toString());
				AddEvent(reason);
				return;
			}

		}
		File file = new File(dir, "BtEvents.txt");
		try(FileWriter fw = new FileWriter(file, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
			DateFormat fmt = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS");
			String started = "Session Start: " + fmt.format(m_startTime);
			String line;
			line = "=============================";
			out.print(line + "\r\n");
			out.print(started + "\r\n");

			for (OneDeviceEvent ode : m_list) {
				out.print(ode.getEvent() + "\r\n");
			}
		} catch (IOException ex) {
			//exception handling left as an exercise for the reader
			String reason = ex.getMessage();
			AddEvent("CAN'T WRITE TO FILE:");
			AddEvent(reason);
		}
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

	private String DataToHex(byte[] b, int length, String indent) {
		String s;
		s = indent + "Data Received, length = " + String.format("%d", length);
		for (int i = 0; i < length; i++) {
			if ((i % 8) == 0) {
				s += "\r\n" + String.format("%04x", i) + "  ";
			}
			else if ((i % 4) == 0) {
				s += " ";
			}
			// bytes and ints are SIGNED, fixup for 'megative' numbers...
			int val = b[i];  // if b[i] is 0x81, then val is 0xffffff80!
			val &= 0xff;
			s += String.format("%02x", val) + " ";
		}
		s += "\r\n";
		return s;
	}

	private boolean EnableGlucoseServiceCharacteristicNotifications() {
		String svc_name;
		List<String> chr_names;

		svc_name = m_uuidlookup.SvcId_GlucoseService;
		chr_names = new ArrayList<>();
		//
		//  16:36:13.349: Getting Glucose Service characteristics...
		//     16:36:13.350: CHR: Glucose Measurement (0x2a18)
		//     16:36:13.351: CHR: Glucose Measurement Context (0x2a34)
		//     16:36:13.352: CHR: Glucose Feature (0x2a51)
		//     16:36:13.353: CHR: Record Access Control Point (0x2a52)
		chr_names.add(m_uuidlookup.ChrId_GlucoseMeasurement);
		chr_names.add(m_uuidlookup.ChrId_GlucoseMeasurementContext);
		chr_names.add(m_uuidlookup.ChrId_GlucoseFeature);
		chr_names.add(m_uuidlookup.ChrId_RecordAccessControlPoint);

		return m_bleAdapter.EnableCharacteristicNotifications(svc_name, chr_names);
	}

	private String statusToString(int gatt_status) {
		String strStatus = "Status: ";
		if (gatt_status == 0) {
			strStatus += "Success";
		}
		else {
			strStatus += "BluetoothGatt." + String.format("%d", gatt_status);
		}
		return strStatus;
	}

	private void handle(Message msg) {
		Bundle bundle;
		boolean found;
		int reason;
		String service_uuid = "";
		String characteristic_uuid = "";
		int status;
		byte[] b = null;
		int length;
		String hex;
		String uuid;
		byte[] racpCommand;

		switch (msg.what) {
			case BleAdapterService.MESSAGE:
				bundle = msg.getData();
				String text = bundle.getString(BleAdapterService.BundleText);
				AddEvent(text);
				break;

			case BleAdapterService.GATT_CONNECTED:
				AddEvent("Connected, calling DiscoverServices()");
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
				// This call is async; we get SERVICES_DISCOVERED shortly...
				m_bleAdapter.discoverServices();
				break;

			case BleAdapterService.GATT_CHARACTERISTIC_READ:
				AddEvent("CHR read.");
				bundle = msg.getData();
				uuid = bundle.getString(BundleUuid);
				// These will be set:
				// BundleHadAnError (boolean)
				//    BundleLastError (optional, if HadAnError)
				// BundleStatus (int) -- Zero == OK
				// BundleUuid (String)
				// BundleRawValueLength (int)
				// BundleRawValue (byte[])
				length = bundle.getInt(BleAdapterService.BundleRawValueLength);
				b = bundle.getByteArray(BleAdapterService.BundleRawValue);
				hex = DataToHex(b, length, "   ");
				// Check that status == BluetoothGatt.GATT_SUCCESS (which is zero)
				status = bundle.getInt(BleAdapterService.BundleStatus);
				AddEvent(uuid + ": Status: " + statusToString(status) + ":\r\n" + hex);
				break;

			case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
				AddEvent("CHR Written");
				bundle = msg.getData();
				uuid = bundle.getString(BundleUuid);
				length = bundle.getInt(BleAdapterService.BundleRawValueLength);
				b = bundle.getByteArray(BleAdapterService.BundleRawValue);
				hex = DataToHex(b, length, "   ");
				status = bundle.getInt(BleAdapterService.BundleStatus);
				AddEvent(uuid + ": Status: " + statusToString(status) + ":\r\n" + hex);
				break;

			case GATT_NOTIFICATION_OR_INDICATION_RECEIVED:
				// This is Characteristic Changed event
				AddEvent("CHR INDICATION/NOTIFICATON RECEIVED.");
				// No 'status' is sent with this notification.
				bundle = msg.getData();
				uuid = bundle.getString(BundleUuid);

				length = bundle.getInt(BleAdapterService.BundleRawValueLength);
				b = bundle.getByteArray(BleAdapterService.BundleRawValue);
				hex = DataToHex(b, length, "   ");
				AddEvent(m_uuidlookup.GetNameWithShortUuid(uuid));
				AddEvent(hex);
				break;

			case BleAdapterService.GATT_DESCRIPTOR_READ:
				AddEvent("DESCRIPTOR READ, Current Value:");
				bundle = msg.getData();
				uuid = bundle.getString(BundleUuid);
				characteristic_uuid = bundle.getString(BleAdapterService.BundleCharacteristicId);
				length = bundle.getInt(BleAdapterService.BundleRawValueLength);
				b = bundle.getByteArray(BleAdapterService.BundleRawValue);
				hex = DataToHex(b, length, "      ");
				status = bundle.getInt(BleAdapterService.BundleStatus);
				AddEvent( "DSC READ: " + m_uuidlookup.GetNameWithShortUuid(uuid));
				AddEvent( "   CHR: " + m_uuidlookup.GetNameWithShortUuid(characteristic_uuid));
				AddEvent( "   Status: " + statusToString(status) + ":");
				AddEvent(hex);
				AddEvent("SettingDescriptorNotifications: GlucoseMeasurement-ClientConfig");
				if (m_bleAdapter.SetDescriptorNotifications(m_uuidlookup.SvcId_GlucoseService,
						m_uuidlookup.ChrId_GlucoseMeasurement,
						m_uuidlookup.DscId_ClientConfigurationConfig)) {
					AddEvent("Submit SUCCESS");
				}
				else {
					AddEvent("Submit: FAILED");
				}
				break;

			case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
				AddEvent("DESCRIPTOR Written, new value:");
				bundle = msg.getData();
				uuid = bundle.getString(BundleUuid);
				characteristic_uuid = bundle.getString(BleAdapterService.BundleCharacteristicId);
				length = bundle.getInt(BleAdapterService.BundleRawValueLength);
				b = bundle.getByteArray(BleAdapterService.BundleRawValue);
				hex = DataToHex(b, length, "   ");
				status = bundle.getInt(BleAdapterService.BundleStatus);
				AddEvent( "DSC WRITTEN: " + m_uuidlookup.GetNameWithShortUuid(uuid));
				AddEvent( "   CHR: " + m_uuidlookup.GetNameWithShortUuid(characteristic_uuid));
				AddEvent( "   Status: " + statusToString(status) + ":");
				AddEvent(hex);
				if (status == 0 && characteristic_uuid != null) {
					if (characteristic_uuid.equalsIgnoreCase(m_uuidlookup.ChrId_GlucoseMeasurement)) {
						AddEvent("SettingDescriptorNotifications: GlucoseMeasurementContext-ClientConfig");
						if (m_bleAdapter.SetDescriptorNotifications(m_uuidlookup.SvcId_GlucoseService,
								m_uuidlookup.ChrId_GlucoseMeasurementContext,
								m_uuidlookup.DscId_ClientConfigurationConfig)) {
							AddEvent("Submit SUCCESS");
						}
						else {
							AddEvent("Submit: FAILED");
						}
					}
					else {
						// Send "Get All" coomand to RACP Characteristic...
						racpCommand = new byte[2];
						racpCommand[0] = 0x01;  // Resport Stored Records
						racpCommand[1] = 0x01;  // All records
						AddEvent("Enabling Report All Records --> RACP....");

						if (m_bleAdapter.SendCharacteristicCommand(m_uuidlookup.SvcId_GlucoseService,
								m_uuidlookup.ChrId_RecordAccessControlPoint,
								racpCommand)) {
							AddEvent("Submit SUCCESS");
						}
						else {
							AddEvent("Submit FAILED: " + m_bleAdapter.LastError);
						}
					}
				}
				break;

			case BleAdapterService.GATT_SERVICES_DISCOVERED:
				found = false;
				// m_deviceServices: List<String>
				AddEvent("Services discovered:");
				if (!m_bleAdapter.GetDeviceServices(m_deviceServices)) {
					AddEvent("Can't get Devices Services!");
					return;
				}
				AddEvent("Device Services: (" + String.format("%d", m_deviceServices.size()));
				for (String s : m_deviceServices) {
					if (s.equals(m_uuidlookup.SvcId_GlucoseService)) {
						found = true;
					}
					String svc_name = m_uuidlookup.GetNameWithShortUuid(s);
					// e.g., "Glucose Service (0x1808)"
					AddEvent("SVC: " + svc_name);
				}

				if (!found) {
					AddEvent("Glucose Service Not detected; not a glucose reader device?");
					return;
				}
				// (else...)
				// Once we have discovered device's services, we know each Service's
				// characteristics and all caracteristic's descriptors; getting these
				// lists is not async...
				/*
				07:39:43.976: SVC: Glucose Service (0x1808)
				07:39:43.977: SVC: Soft Serial Service (0xfff0)
				07:39:43.977: Getting Glucose Service characteristics...
				07:39:43.979: CHR: Glucose Measurement (0x2a18)
				07:39:43.980: CHR: Glucose Measurement Context (0x2a34)
				07:39:43.981: CHR: Glucose Feature (0x2a51)
				07:39:43.982: CHR: Record Access Control Point (0x2a53)

				 */
				AddEvent("Getting Glucose Service characteristics...");
				if (!m_bleAdapter.GetServicesCharacteristics(m_uuidlookup.SvcId_GlucoseService,
						m_serviceCharacteristics)) {
					AddEvent("Could not get Glucose Service's characteristics.");
					return;
				}
				// (else...)
				// At this point, the BT stack "knows" all of the device's Services, and
				// for each Service, all of that Service's Characteristics, and all of
				// Characteristic's Descriptors.
				// (Just a guess but $20 says the device sends up this stuff as an XmlDoc
				//    very much like bluetooth.org documents.)
				for (String s : m_serviceCharacteristics) {
					String chr_name = m_uuidlookup.GetNameWithShortUuid(s);
					AddEvent("CHR: " + chr_name);
				}
				// Get descriptors for all CHRs: 0x2a18, 2a34, 2a51, 2a53
				for (String s : m_serviceCharacteristics) {
					List<String> list = new ArrayList<>();
					String chr_name = m_uuidlookup.GetNameWithShortUuid(s);
					if (!m_bleAdapter.GetCharacteristicsDescriptors(m_uuidlookup.SvcId_GlucoseService, s, list)) {
						AddEvent("Can't get Descriptors for: " + chr_name);
					}
					else {
						AddEvent("DESCs of " + chr_name + ": Count = " + String.format("%d", list.size()));
						for (String d : list){
							String dsc = m_uuidlookup.GetNameWithShortUuid(d);
							AddEvent("    " + dsc);
						}
					}
				}
				if (EnableGlucoseServiceCharacteristicNotifications()) {
					AddEvent("=== Enable Glu Svc Notifications(): SUCCESS");
				}
				else {
					AddEvent("=== !! Enable Glu Svc Notifications() FAILED: " + m_bleAdapter.LastError);
					return;
				}
				AddEvent("Getting Client Config DSC...");
				if (m_bleAdapter.ReadDescriptor(m_uuidlookup.SvcId_GlucoseService,
						m_uuidlookup.ChrId_GlucoseMeasurement,
						m_uuidlookup.DscId_ClientConfigurationConfig)) {
					AddEvent("Submit SUCCESS");
				}
				else {
					AddEvent("Submit: FAILED");
				}

				break;

			case BleAdapterService.GATT_DISCONNECT:
				bundle = msg.getData();
				reason = bundle.getInt(BleAdapterService.BundleStatus, 0);
//				((Button) PeripheralControlActivity.this
//						.findViewById(R.id.connectButton)).setEnabled(true);
				// we're disconnected
				AddEvent("DISCONNECTED (" + String.format("%d", reason) + ")");
//  			if (back_requested) {
//	    			PeripheralControlActivity.this.finish();
//		    	}
				break;
		}
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
			// Set ActivityHandler and UUIL lookup:
			m_bleAdapter.SetHandlerAndUuidLookup(m_msgHandler, m_uuidlookup);
            //   m_bleAdapter.setActivityHandler(m_msgHandler);
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			m_bleAdapter = null;
		}
	};

    // MsgHandler class avoids this warning:
    // "This Handler class should be static or leaks might occur: message_handler"
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
	        m_parent = new WeakReference<>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
	        DeviceEventActivity01 parent = m_parent.get();
	        if (parent == null) {
	            return;
            }
	        parent.handle(msg);
        }  // class MsgHandler
    }
}
