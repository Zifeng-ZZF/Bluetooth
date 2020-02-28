package com.example.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ListView;

import com.unity3d.player.UnityPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BluetoothConnector {
//    private static BluetoothConnector instance;
//    public static BluetoothConnector getInstance() {
//        if (instance != null) {
//            return instance;
//        } else {
//            instance = new BluetoothConnector();
//            return instance;
//        }
//    }
//    private BluetoothConnector() {};
    
    private static class Holder {
        public static BluetoothConnector mInstance = new BluetoothConnector();
    }
    public static BluetoothConnector getInstance() {
        return Holder.mInstance;
    }

    private Activity mainActivity;
    private ArrayList<BluetoothDevice> myDevices = new ArrayList<>();
    private ArrayList<BluetoothDevice> nearbyDevices = new ArrayList<>();
    private Map<BluetoothDevice, Short> deviceSignalMap = new HashMap<>();

    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter adapter;
    private BluetoothDevice curDeivce;
    private ConnectThread connectThread;

    private boolean isDiscoverFinished = false;

    /**
     * Initializing bluetooth utility
     */
    public void initialize() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            myDevices.add(device);
        }
        mainActivity = UnityPlayer.currentActivity;
    }

    /**
     * Get a list of paired devices in the form of a json string
     * @return list of bonded devices
     */
    public String getPairedDevices(){
        JSONObject deviceList = new JSONObject();
        JSONArray deviceArr = new JSONArray();
        for (BluetoothDevice device : myDevices) {
            JSONObject bluetoothDevice = null;
            try {
                bluetoothDevice = new JSONObject();
                bluetoothDevice.put("name", device.getName());
                bluetoothDevice.put("address", device.getAddress());

                JSONArray uuidsArr = new JSONArray();
                for (ParcelUuid uuid : device.getUuids()) {
                    uuidsArr.put(uuid.toString());
                }
                bluetoothDevice.put("uuids", uuidsArr);
            } catch (JSONException e){
                e.printStackTrace();
            }

            deviceArr.put(bluetoothDevice);
        }

        try {
            deviceList.put("deviceArray", deviceArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return deviceList.toString();
    }

    /**
     * receiver
     */
    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (nearbyDevices.contains(device)){
                    return;
                }
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                    deviceSignalMap.put(device, rssi);
                    nearbyDevices.add(device);
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                isDiscoverFinished = true;
            }
        }
    };

    /**
     * Get nearby devices while the discover process is complete
     */
    public void startDiscover(Object context) {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ((Context)context).registerReceiver(myReceiver, filter);
        Log.d("zzf", "startDiscover: android start discovering");
        adapter.startDiscovery();
    }

    /**
     * Cancel discovery
     */
    public void cancelDiscover() {
        adapter.cancelDiscovery();
    }

    /**
     * @return json string containing a list of the devices
     */
    public String processNearDevicesInfo() {
        JSONObject deviceList = null;
        if (nearbyDevices.size() != 0) {
            deviceList = new JSONObject();
            JSONArray deviceArr = new JSONArray();
            for (BluetoothDevice device : nearbyDevices) {
                JSONObject bluetoothDevice = null;
                try {
                    bluetoothDevice = new JSONObject();
                    bluetoothDevice.put("name", device.getName());
                    bluetoothDevice.put("address", device.getAddress());
                    bluetoothDevice.put("rssi", deviceSignalMap.get(device));
                } catch (JSONException e){
                    e.printStackTrace();
                }
                deviceArr.put(bluetoothDevice);
            }

            try {
                deviceList.put("deviceArray", deviceArr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("zzf", "processNearDevicesInfo: no nearby devices");
        }

        if (deviceList == null) {
            return "still searching";
        }
        return deviceList.toString();
    }

    /**
     *
     * @return whether the process is finished
     */
    public boolean hasDiscoveredFinished() {
        Log.d("zzf", "hasDiscoveredFinished: " + isDiscoverFinished);
        return isDiscoverFinished;
    }

    /**
     * Connect to a device by address
     * @param address MAC address of the device intended to connect
     * @return whether connection is established
     */
//    public boolean connectDevice(String address) {
//        //Cancel discovery process before connecting
//        adapter.cancelDiscovery();
//
//        BluetoothDevice tempDev = null;
//
//        for (int i = 0; i < myDevices.size(); i++) {
//            if (myDevices.get(i).getAddress().equals(address)) {
//                Log.d("zzf", "connectDevice: matched list");
//                tempDev = myDevices.get(i);
//                break;
//            }
//        }
//
//        if (tempDev == null) {
//            for (int i = 0; i < nearbyDevices.size(); i++) {
//                if (nearbyDevices.get(i).getAddress().equals(address)) {
//                    Log.d("zzf", "connectDevice: matched list");
//                    tempDev = nearbyDevices.get(i);
//                    break;
//                }
//            }
//        }
//
//        if (tempDev != null) {
//            connectThread = new ConnectThread(tempDev);
//            connectThread.start();
//            return true;
//        }
//        return false;
//    }

    /**
     * Disconnect the current device
     */
//    public void disconnectDevice() {
//        if (connectThread == null || curDeivce == null) return;
//        connectThread.cancel();
//        curDeivce = null;
//    }

    /**
     * Get the currently connected device by converting the device info into a json
     * @return the json of the device info
     */
    public String getCurrentDeivce() {
        JSONObject current = null;
        curDeivce = getConnected();
        if (curDeivce == null)
        {
            return null;
        }
        try {
            current = new JSONObject();
            current.put("name", curDeivce.getName());
            current.put("address", curDeivce.getAddress());

            JSONArray uuidsArr = new JSONArray();
            for (ParcelUuid uuid : curDeivce.getUuids()) {
                uuidsArr.put(uuid.toString());
            }
            current.put("uuids", uuidsArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (current == null) return "";
        return current.toString();
    }

    /**
     * Get the currently connected BluetoothDevice using reflection
     * @return connected device
     */
    private BluetoothDevice getConnected() {
        try {
            Method stateMethod = BluetoothAdapter.class.getDeclaredMethod("getConnectionState", null);
            stateMethod.setAccessible(true);
            int state = (int) stateMethod.invoke(adapter, null);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Log.i("zzf", "getConnected: STATE_CONNECTED");
                for (BluetoothDevice bd : pairedDevices) {
                    Method isConnectMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", null);
                    isConnectMethod.setAccessible(true);
                    boolean isConnected = (boolean) isConnectMethod.invoke(bd, null);
                    if (isConnected) {
                        Log.i("zzf", "getConnected: connected device: " + bd.getAddress());
                        return bd;
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Destroy the data
     * @param context
     */
    public void Destroy(Object context)
    {
        connectThread.cancel();
        adapter.cancelDiscovery();
        ((Context)context).unregisterReceiver(myReceiver);
    }
}