package com.example.bluetoothtester;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothConnector {
    private static BluetoothConnector instance = new BluetoothConnector();
    public static BluetoothConnector getInstance() {
        return instance;
    }

    private ArrayList<BluetoothDevice> myDevices = new ArrayList<>();

    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter adapter;
    private ArrayList<BluetoothDevice> nearbyDeivces = new ArrayList<>();
    private BluetoothSocket socket;
    private BluetoothDevice curDeivce;

    private ListView nearLv;
    private MainActivity.MyAdapter myAdapter;

    private BluetoothConnector() {}

    public ArrayList<BluetoothDevice> getDevicesList() {
        if ((adapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Log.d("zzf", "getDevicesList: no adapter");
            return null;
        }

        if ((pairedDevices = adapter.getBondedDevices()) == null) {
            Log.d("zzf", "getDeviceList: no paired device");
            return null;
        }

        for (BluetoothDevice device : pairedDevices) {
            myDevices.add(device);
        }

        return myDevices;
    }

    public void getDiscoveredDevices(Context context, ListView lv, MainActivity.MyAdapter myAdapter) {
        nearLv =lv;
        this.myAdapter = myAdapter;

        Log.d("zzf", "getDiscoveredDevices: register receiver");

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(myReceiver, filter);
        adapter.startDiscovery();
    }

    public void destroy(Context context) {
        context.unregisterReceiver(myReceiver);
        adapter.cancelDiscovery();
    }

    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("zzf", "onReceive: receive " + intent.getAction());
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("zzf", "onReceive: found " + device.getName());
                if(nearbyDeivces.contains(device)){
                    Log.d("zzf", "onReceive: same device");
                    return;
                }
                nearbyDeivces.add(device);
                myAdapter.setData(nearbyDeivces);
                nearLv.setAdapter(myAdapter);
            }
        }
    };

    public void readData() {

    }
}