package com.example.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {

    private BluetoothSocket mySocket;
    private BluetoothDevice connectDevice;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // well-known SPP UUID

    public ConnectThread(BluetoothDevice device) {
        mySocket = null;
        connectDevice = device;
        uuid = device.getUuids()[0].getUuid();

        try {
            mySocket = connectDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        try {
            mySocket.connect();
        } catch (IOException e) {
            Log.e("zzf", "run: connect fail", e);
            try {
                mySocket.close();
            } catch (IOException ex) {
                Log.e("zzf", "run: cannot close socket", ex);
            }
            return;
        }

        manageConnectedSocket(mySocket);
        Log.d("zzf", "run: connect succesfully");
    }

    public void cancel() {
        try {
            mySocket.close();
        } catch (IOException ex) {
            Log.e("zzf", "run: cannot close socket", ex);
        }
    }

    public void manageConnectedSocket(BluetoothSocket connectedSkt) {
        //TODO
    }

    public BluetoothDevice getConnectDevice() {
        if (!mySocket.isConnected() || mySocket == null) {
            return null;
        }
        return mySocket.getRemoteDevice();
    }
}
