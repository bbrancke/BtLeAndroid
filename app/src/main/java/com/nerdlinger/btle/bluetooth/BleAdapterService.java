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

import java.lang.reflect.Method;
import java.util.List;

public class BleAdapterService extends Service {

	private BluetoothAdapter bluetooth_adapter;
	private BluetoothGatt bluetooth_gatt;
	private BluetoothManager bluetooth_manager;
	private Handler activity_handler = null;
	private BluetoothDevice device;
	private BluetoothGattDescriptor descriptor;
	private List<BluetoothGattService> m_deviceServices;
	private List<BluetoothGattCharacteristic> m_serviceCharacteristics;
	private List<BluetoothGattDescriptor> m_characteristicsDescriptors;
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
	public static final int NOTIFICATION_OR_INDICATION_RECEIVED = 8;
	// message parms
	public static final String PARCEL_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
	public static final String PARCEL_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
	public static final String PARCEL_SERVICE_UUID = "SERVICE_UUID";
	public static final String PARCEL_VALUE = "VALUE";
	public static final String PARCEL_RSSI = "RSSI";
	public static final String PARCEL_TEXT = "TEXT";
	public static final String PARCEL_STATUS = "STATUS";

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

	// Activity calls this to register to receive my messages:
	public void setActivityHandler(Handler handler) {
		activity_handler = handler;
	}

	private void sendConsoleMessage(String text) {
		Message msg = Message.obtain(activity_handler, MESSAGE);
		Bundle data = new Bundle();
		data.putString(PARCEL_TEXT, text);
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

	public boolean GetDeviceServices(List<String> list) {
		list.clear();
		if (bluetooth_gatt == null) {
			return false;
		}
		m_deviceServices = bluetooth_gatt.getServices();
		for (BluetoothGattService svc : m_deviceServices) {
			list.add(svc.getUuid().toString().toLowerCase());
		}
		return true;
	}

	private BluetoothGattService getServiceByUuid(String uuid) {
		for (BluetoothGattService svc : m_deviceServices) {
			if (uuid.equals(svc.getUuid().toString())) {
				return svc;
			}
		}
		return null;
	}

	public boolean GetServicesCharacteristics(String uuid, List<String> list) {
		BluetoothGattService svc;
		list.clear();
		svc = getServiceByUuid(uuid);
		if (svc == null) {
			return false;
		}
		m_serviceCharacteristics = svc.getCharacteristics();
		for (BluetoothGattCharacteristic chr : m_serviceCharacteristics) {
			list.add(chr.getUuid().toString().toLowerCase());
		}
		return true;
	}

	private BluetoothGattCharacteristic getCharacteristicByUuid(String uuid) {
		for (BluetoothGattCharacteristic chr : m_serviceCharacteristics) {
			if (uuid.equals(chr.getUuid().toString())) {
				return chr;
			}
		}
		return null;
	}

	public List<BluetoothGattDescriptor> getCharacteristicsDescriptors(String uuid) {
		BluetoothGattCharacteristic chr;
		chr = getCharacteristicByUuid(uuid);
		if (chr == null) {
			return null;
		}
		m_characteristicsDescriptors = chr.getDescriptors();
		return m_characteristicsDescriptors;
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
			bundle.putInt(PARCEL_STATUS, status);
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
