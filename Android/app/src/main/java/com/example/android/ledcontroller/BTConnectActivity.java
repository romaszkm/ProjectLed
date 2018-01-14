package com.example.android.ledcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * Created by michalromaszko on 14.01.2018.
 */

public class BTConnectActivity extends AppCompatActivity {

    private static final String DEVICE = "raspberrypi";
    public final static int REQUEST_ENABLE_BT = 1;
    public final static String UUID = "f4eee4c3-8d72-479d-b6ec-41e960ad0967";
    private static BluetoothSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableBT();
    }

    public static void set(Room room) {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(room.toJson().toString().getBytes());
        } catch (IOException e) {
            Log.e("BT", "Error occurred when sending data", e);
        }
    }

    private String getRooms() {
        try {
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[1024];
            is.read(buffer);
            String s = new String(buffer, "UTF-8");
            return s;
        } catch (Exception e) {
            Log.e("BT", "Error occurred when receiving data", e);
        }
        return null;
    }

    private void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BTConnectActivity.REQUEST_ENABLE_BT);
            } else {
                onActivityResult(REQUEST_ENABLE_BT, RESULT_OK, null);
            }
        }
    }

    private void startMainActivity(String json) {
        Intent intent = new Intent(this, MainActivity.class);
        MainActivity.mode = MainActivity.BT_MODE;
        intent.putExtra("JSON", json);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BTConnectActivity.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                BluetoothDevice rpi = getRpi();
                setSocket(getRpi());
                startMainActivity(getRooms());
            }
        }
    }

    private BluetoothDevice getRpi() {
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if (deviceName.equals(DEVICE))
                    return device;
            }
        }
        return null;
    }

    private void setSocket(BluetoothDevice device) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
            mBluetoothAdapter.cancelDiscovery();
            tmp.connect();
        } catch (Exception e) {
            Log.e("BT", "Error occurred when opening socket", e);
            if (tmp != null) {
                try {
                    tmp.close();
                } catch (Exception e1) {
                }
            }
        }
        socket = tmp;
    }

}
