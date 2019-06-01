package com.nerdlinger.btle.bluetooth;
// Bluetooth LE Developer Study Guide,
//    Exercise 2 - Communicating with the Peripheral Device, page 42.
// BleAdaptorService - AFTER Scan, this connects to a particular device,
//   discovers the services that this device provides, etc.
//   This does not use the BluetoothAdaoter that the Scan operation used,
//   don't know why. And the 'Constants' class is nowhere to be found in here...
// This is a service that talks to the GUI using Handler, Message and Bundle
// Handler: I set up and GUI registers with my Handler.
// I send Message's which contain a Bundle.
// Bundle has an (int) 'Type' and (data) - UI knows data type based on 'Type'
//
// "All calls into Bluetooth Stack ("BS") should be from the same thread,
//   preferably the main (UI) thread."
// 1. User taps Connect --> UI --> BleAdapterSvc->[BS]
//    -- async call, very shortly:
//    [BS]--> BleAdapterSvc --> UI --> "CONNECTED"
// 2. UI calls Discover Services (another aync)
//    shortly we get ServicesDiscovered event;
//    UI can then call getBTServices() [ a List of Svcs]
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.nerdlinger.btle.Constants;
import com.nerdlinger.btle.ui.UuidLookup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleAdapterService extends Service {

	private BluetoothAdapter bluetooth_adapter;
	private BluetoothGatt bluetooth_gatt;
	private BluetoothManager bluetooth_manager;
	private Handler activity_handler = null;
	private BluetoothDevice device;
	private BluetoothGattDescriptor descriptor;
//	private List<BluetoothGattService> m_deviceServices;
//	private List<BluetoothGattCharacteristic> m_serviceCharacteristics;
//	private List<BluetoothGattDescriptor> m_characteristicsDescriptors;
	private boolean connected = false;
	public boolean alarm_playing = false;

	// messages sent back to activity
	public static final int GATT_CONNECTED = 1;
	public static final int GATT_DISCONNECT = 2;
	public static final int GATT_SERVICES_DISCOVERED = 3;
	public static final int GATT_CHARACTERISTIC_READ = 4;
	public static final int GATT_CHARACTERISTIC_WRITTEN = 5;
	public static final int GATT_REMOTE_RSSI = 6;
	public static final int MESSAGE = 7;
	public static final int GATT_NOTIFICATION_OR_INDICATION_RECEIVED = 8;
	public static final int GATT_DESCRIPTOR_READ = 9;
	public static final int GATT_DESCRIPTOR_WRITTEN = 10;
	// message params
	public static final String PARCEL_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
	public static final String PARCEL_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
	public static final String PARCEL_SERVICE_UUID = "SERVICE_UUID";
	public static final String PARCEL_VALUE = "VALUE";
	public static final String PARCEL_RSSI = "RSSI";
	public static final String BundleText = "TEXT";
	public static final String BundleStatus  = "STATUS";
	public static final String BundleUuid = "UUID";
	public static final String BundleHadAnError = "HAD_AN_ERROR";
	public static final String BundleLastError = "LAST_ERROR";
	public static final String BundleRawValue = "RAW_DATA";
	public static final String BundleRawValueLength = "RAW_DATA_LENGTH";
	public static final String BundleCharacteristicId = "CHARACTERISTIC_UUID";

	public static String IMMEDIATE_ALERT_SERVICE_UUID = "00001802-0000-1000-8000-00805F9B34FB";
	public static String LINK_LOSS_SERVICE_UUID = "00001803-0000-1000-8000-00805F9B34FB";
	public static String TX_POWER_SERVICE_UUID = "00001804-0000-1000-8000-00805F9B34FB";
	public static String PROXIMITY_MONITORING_SERVICE_UUID = "3E099910-293F-11E4-93BD-AFD0FE6D1DFD";
	public static String HEALTH_THERMOMETER_SERVICE_UUID = "00001809-0000-1000-8000-00805F9B34FB";
	// service characteristics
	public static String ALERT_LEVEL_CHARACTERISTIC = "00002A06-0000-1000-8000-00805F9B34FB";
	public static String CLIENT_PROXIMITY_CHARACTERISTIC = "3E099911-293F-11E4-93BD-AFD0FE6D1DFD";
	public static String TEMPERATURE_MEASUREMENT_CHARACTERISTIC = "00002A1C-0000-1000-8000-00805F9B34FB";
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	private UuidLookup m_uuidLookup;
	public String LastError = "";

	// Activity calls this to register to receive my messages and sets UuidLookup:
	public void SetHandlerAndUuidLookup(Handler handler, UuidLookup uuidLookup) {
		activity_handler = handler;
		m_uuidLookup = uuidLookup;
	}

	private void sendConsoleMessage(String text) {
		Message msg = Message.obtain(activity_handler, MESSAGE);
		Bundle data = new Bundle();
		data.putString(BundleText, text);
		msg.setData(data);
		msg.sendToTarget();
	}

	// Clear the saved Services etc for this device:
	private boolean refreshDeviceCache() {
		try {
			BluetoothGatt localGatt = bluetooth_gatt;
			Method localMethod = localGatt.getClass().getMethod("refresh", new Class[0]);
			if (localMethod == null) {
				log("refreshDeviceCache(): Method refresh() not found");
				return false;
			}
			boolean b = ((Boolean) localMethod.invoke(localGatt, new Object[0])).booleanValue();
			return b;
		}
		catch (Exception ex) {
			log("refreshDeviceCache(): " + ex.getMessage());
			return false;
		}
	}
	// peripheral activity UI calls connect() and disconnect():
	public boolean connect(final String address) {
		if (bluetooth_adapter == null || address == null) {
			sendConsoleMessage("connect() failed: bt adaptor or address is null");
			return false;
		}
		device = bluetooth_adapter.getRemoteDevice(address);
		if (device == null) {
			sendConsoleMessage("Connect() failed: device=null. Off or out of range?");
			return false;
		}
		bluetooth_gatt = device.connectGatt(this, false, gatt_callback);
		refreshDeviceCache();

		return true;
	}

	public void disconnect() {
		sendConsoleMessage("disconnecting");
		if (bluetooth_adapter == null || bluetooth_gatt == null) {
			sendConsoleMessage("disconnect: bluetooth_adapter|bluetooth_gatt null");
			return;
		}

		bluetooth_gatt.disconnect();
	}

	public void discoverServices() {
		if (bluetooth_adapter == null || bluetooth_gatt == null) {
			return;
		}
		log("Discovering GATT services");
		bluetooth_gatt.discoverServices();
	}

	private boolean FillBundleFromDescriptor(Bundle bundle, BluetoothGattDescriptor dsc) {
		BluetoothGattCharacteristic chr;
		bundle.putBoolean(BundleHadAnError, false);
		UUID uuid = dsc.getUuid();
		String sId = uuid.toString().toLowerCase();
		bundle.putString(BundleUuid, sId);

		chr = dsc.getCharacteristic();
		uuid = chr.getUuid();
		sId = uuid.toString().toLowerCase();
		bundle.putString(BundleCharacteristicId, sId);

		byte[] b = dsc.getValue();
		int length = b.length;
		bundle.putInt(BundleRawValueLength, length);
		if (length > 0) {
			bundle.putByteArray(BundleRawValue, b);
		}

		return true;
	}

	private boolean FillBundleFromCharacteristic(Bundle bundle, BluetoothGattCharacteristic chr) {
		// chr has methods such as:
		//      public String getStringValue (int offset)
		// but have to known the offset.
		// TODO: Get Characteristic and Descriptor definitions, add here.
		bundle.putBoolean(BundleHadAnError, false);
		UUID uuid = chr.getUuid();
		String sId = uuid.toString().toLowerCase();
		bundle.putString(BundleUuid, sId);

		byte[] b = chr.getValue();
		int length = b.length;
		bundle.putInt(BundleRawValueLength, length);
		if (length > 0) {
			bundle.putByteArray(BundleRawValue, b);
		}
		// TODO: Parse the Characteristic by type, fill in values ==> bundle.
		// FOR NOW, just show the raw data... Maybe the UI should parse this raw data...
		if (sId.equalsIgnoreCase(m_uuidLookup.ChrId_GlucoseMeasurement)) {
			// I think we will use something like:
			//     return FillBundleFromGlugoseMeasurement(bundle, chr);
			// here...
			return true;
		}
		// (else)...
		if (sId.equalsIgnoreCase(m_uuidLookup.ChrId_GlucoseMeasurementContext)) {
			return true;
		}
		// (else)...
		if (sId.equalsIgnoreCase(m_uuidLookup.ChrId_GlucoseFeature)) {
			return true;
		}
		// (else)...
		if (sId.equalsIgnoreCase(m_uuidLookup.ChrId_RecordAccessControlPoint)) {
			return true;
		}
		// else...
		// unrecognized characteristic
		bundle.putBoolean(BundleHadAnError, true);
		bundle.putString(BundleLastError, "Unrecognized Characteristic (might be optional)");
		return false;
	}

	public boolean GetDeviceServices(List<String> list) {
		List<BluetoothGattService> deviceServices;
		list.clear();
		if (bluetooth_gatt == null) {
			return false;
		}
		deviceServices = bluetooth_gatt.getServices();
		for (BluetoothGattService svc : deviceServices) {
			list.add(svc.getUuid().toString().toLowerCase());
		}
		return true;
	}
// List<BluetoothGattCharacteristic> m_serviceCharacteristics;
//	private List<BluetoothGattDescriptor> m_characteristicsDescriptors;
//	private List<BluetoothGattService> m_deviceServices;
	private BluetoothGattService getServiceByUuid(String svc_uuid) {
		UUID uuid;

		uuid = UUID.fromString(svc_uuid);
		return bluetooth_gatt.getService(uuid);
	}

	public boolean GetServicesCharacteristics(String uuid, List<String> list) {
		BluetoothGattService svc;
		List<BluetoothGattCharacteristic> svc_characteristics;

		list.clear();
		svc = getServiceByUuid(uuid);
		if (svc == null) {
			return false;
		}
		svc_characteristics = svc.getCharacteristics();
		for (BluetoothGattCharacteristic chr : svc_characteristics) {
			list.add(chr.getUuid().toString().toLowerCase());
		}
		return true;
	}

	public boolean GetCharacteristicsDescriptors(String svc_uuid, String chr_uuid, List<String> list) {
		BluetoothGattService svc;
		BluetoothGattCharacteristic chr;
		List<BluetoothGattDescriptor> descs;
		UUID uuid;

		list.clear();
		svc = getServiceByUuid(svc_uuid);
		if (svc == null) {
			return false;
		}

		uuid = UUID.fromString(chr_uuid);
		chr = svc.getCharacteristic(uuid);
		if (chr == null) {
			return false;
		}

		descs = chr.getDescriptors();
		for (BluetoothGattDescriptor desc : descs) {
			list.add(desc.getUuid().toString().toLowerCase());
		}
		return true;
	}

	private void AppendLastError(String msg) {
		if (LastError.length() > 0) {
			LastError += "\r\n";
		}
		LastError += msg;
	}

	private BluetoothGattService GetService(String svc_name) {
		UUID uuid;
		BluetoothGattService svc;

		uuid = UUID.fromString(svc_name);
		svc = bluetooth_gatt.getService(uuid);
		if (svc == null) {
			AppendLastError("Can't get Service ID: " + svc_name);
		}
		return svc;
	}

	private BluetoothGattCharacteristic GetCharacteristic(String svc_name, String chr_name) {
		BluetoothGattService svc;
		BluetoothGattCharacteristic chr;
		UUID uuid;
		svc = GetService(svc_name);
		if (svc == null) {
			return null;
		}
		uuid = UUID.fromString(chr_name);
		chr = svc.getCharacteristic(uuid);
		if (chr == null) {
			AppendLastError("Can't get characteristic: " + chr_name + ", continuing...");
		}
		return chr;
	}

	private BluetoothGattDescriptor GetDescriptor(String svc_name, String chr_name, String dsc_name) {
		BluetoothGattCharacteristic chr;
		BluetoothGattDescriptor dsc;
		UUID uuid;

		chr = GetCharacteristic(svc_name, chr_name);
		if (chr == null ) {
			return null;
		}

		uuid = UUID.fromString(dsc_name);
		dsc = chr.getDescriptor(uuid);
		if (dsc == null) {
			AppendLastError("Can't get descriptor: " + dsc_name);
		}
		return dsc;
	}

	public boolean ReadDescriptor(String svc_name, String chr_name, String dsc_name) {
		BluetoothGattDescriptor dsc;

		LastError = "";
		dsc = GetDescriptor(svc_name, chr_name, dsc_name);
		if (dsc == null) {
			return false;
		}
		if (!bluetooth_gatt.readDescriptor(dsc)) {
			AppendLastError("Read Descriptor failed.");
			return false;
		}
		// Now caller should wait for GATT_DESCRIPTOR_READ
		return true;
	}

	public boolean SendCharacteristicCommand(String svc_name, String chr_name,
	                                         byte[] command) {
		BluetoothGattCharacteristic chr;

		LastError = "";

		chr = GetCharacteristic(svc_name, chr_name);
		if (chr == null) {
			return false;
		}
		chr.setValue(command);
		if (!bluetooth_gatt.writeCharacteristic(chr))
		{
			LastError = "SendCharacteristicCommand(): Submit FAILED";
			return false;
		}
		// (else...)
		return true;
	}

	public boolean SetDescriptorNotifications(String svc_name, String chr_name, String dsc_name) {
		BluetoothGattDescriptor dsc;

		LastError = "";
		dsc = GetDescriptor(svc_name, chr_name, dsc_name);
		if (dsc == null) {
			return false;
		}
		dsc.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		dsc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		if (!bluetooth_gatt.writeDescriptor(dsc)) {
			AppendLastError("Write Descriptor failed.");
			return false;
		}
		// Now caller should wait for GATT_DESCRIPTOR_WRITTEN
		return true;
	}

	public boolean EnableCharacteristicNotifications(String svc_name, List<String> chr_names) {
		BluetoothGattCharacteristic chr;
		boolean enabled;
		boolean rv;

		LastError = "";

		enabled = true;
		rv = true;
		for (String chr_name : chr_names) {
			chr = GetCharacteristic(svc_name, chr_name);
			if (chr == null) {
				rv = false;
			}
			else {
				if (!bluetooth_gatt.setCharacteristicNotification(chr, enabled)) {
					AppendLastError("Can't enable Notifications for: " + chr_name + ", continuing...");
					rv = false;
				}
			}
		}
		return rv;
	}

	private void log(String msg)
	{
		Log.e(Constants.TAG, "BleAdapterSvc: " + msg);
	}

	private final BluetoothGattCallback gatt_callback = new BluetoothGattCallback() {
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			sendConsoleMessage("Services Discovered");
			Message msg = Message.obtain(activity_handler,
					GATT_SERVICES_DISCOVERED);
			msg.sendToTarget();
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
		                                    int newState) {
			log("onConnectionStateChange: status=" + status);
			Bundle bundle = new Bundle();
			bundle.putInt(BundleStatus, status);
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				log("onConnectionStateChange: CONNECTED");
				connected = true;
				Message msg = Message.obtain(activity_handler, GATT_CONNECTED);
				msg.setData(bundle);
				msg.sendToTarget();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				log("onConnectionStateChange: DISCONNECTED");
				connected = false;
				Message msg = Message.obtain(activity_handler, GATT_DISCONNECT);
				msg.setData(bundle);
				msg.sendToTarget();
				if (bluetooth_gatt != null) {
					log("Closing and destroying BluetoothGatt object");
					// Note: when a connection is lost, it’s important to
					// close the BluetoothGatt object and ensure it is
					// garbage collected by setting it to null.
					bluetooth_gatt.close();
					bluetooth_gatt = null;
				}
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
		                                 BluetoothGattCharacteristic characteristic,
		                                 int status) {
			Message msg = Message.obtain(activity_handler, GATT_CHARACTERISTIC_READ);
			Bundle bundle = new Bundle();
			bundle.putInt(BundleStatus, status);
			FillBundleFromCharacteristic(bundle, characteristic);
			msg.setData(bundle);
			msg.sendToTarget();
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
		                                  BluetoothGattCharacteristic characteristic, int status) {
			Message msg = Message.obtain(activity_handler, GATT_CHARACTERISTIC_WRITTEN);
			Bundle bundle = new Bundle();
			bundle.putInt(BundleStatus, status);
			FillBundleFromCharacteristic(bundle, characteristic);
			msg.setData(bundle);
			msg.sendToTarget();
		};

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
		                                    BluetoothGattCharacteristic characteristic) {
			Message msg = Message.obtain(activity_handler, GATT_NOTIFICATION_OR_INDICATION_RECEIVED);
			Bundle bundle = new Bundle();
			FillBundleFromCharacteristic(bundle, characteristic);
			msg.setData(bundle);
			msg.sendToTarget();
		}

		@Override
		public void onDescriptorRead (BluetoothGatt gatt,
		                              BluetoothGattDescriptor descriptor,
		                              int status) {
			Message msg = Message.obtain(activity_handler, GATT_DESCRIPTOR_READ);
			Bundle bundle = new Bundle();
			bundle.putInt(BundleStatus, status);
			FillBundleFromDescriptor(bundle, descriptor);
			msg.setData(bundle);
			msg.sendToTarget();
		}

		@Override
		public void onDescriptorWrite (BluetoothGatt gatt,
		                               BluetoothGattDescriptor descriptor,
		                               int status) {
			// if (status == BluetoothGatt.GATT_SUCCESS )  // == zero
			Message msg = Message.obtain(activity_handler, GATT_DESCRIPTOR_WRITTEN);
			Bundle bundle = new Bundle();
			bundle.putInt(BundleStatus, status);
			FillBundleFromDescriptor(bundle, descriptor);
			msg.setData(bundle);
			msg.sendToTarget();
		}
	};

	public boolean isConnected() {
		return connected;
	}

	private final IBinder binder = new LocalBinder();
	public class LocalBinder extends Binder {
		public BleAdapterService getService() {
			return BleAdapterService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		if (bluetooth_manager == null) {
			bluetooth_manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (bluetooth_manager == null) {
				return;
			}
		}
		bluetooth_adapter = bluetooth_manager.getAdapter();
		if (bluetooth_adapter == null) {
			return;
		}
	}
}
