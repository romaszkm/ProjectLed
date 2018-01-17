package com.example.android.ledcontroller.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.android.ledcontroller.ChoiceActivity;
import com.example.android.ledcontroller.ColorActivity;
import com.example.android.ledcontroller.misc.MyAbstractActivity;
import com.example.android.ledcontroller.R;
import com.example.android.ledcontroller.misc.Room;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * Created by michalromaszko on 14.01.2018.
 */

public class BTConnectActivity extends MyAbstractActivity {

    private final static int RESPONSE_TIMEOUT = 1000;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static String UUID = "f4eee4c3-8d72-479d-b6ec-41e960ad0967";
    private static BluetoothSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        enableBT();
    }

    private void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showAlertDialog(this, "Bluetooth not available",
                    "This device does not support bluetooth. Maybe choose internet connection?");
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // calls onActivityResult when BT gets enabled
                startActivityForResult(enableBtIntent, BTConnectActivity.REQUEST_ENABLE_BT);
            } else {
                onActivityResult(REQUEST_ENABLE_BT, RESULT_OK, null);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BTConnectActivity.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                listBondedDevices();
            }
        }
    }

    private void listBondedDevices() {
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_BT);
        View.OnClickListener listener = getChoiceListener(pairedDevices);

        for (BluetoothDevice device : pairedDevices) {
            Button b = new Button(this);
            b.setText(device.getName());
            b.setOnClickListener(listener);
            b.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(b);
        }
    }

    private View.OnClickListener getChoiceListener(final Set<BluetoothDevice> pairedDevices) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSocket();
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    showAlertDialog(BTConnectActivity.this, "Bluetooth error", "Please re-enable bluetooth");
                    return;
                }
                showConnectDialog(BTConnectActivity.this);
                final View view = v;
                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().equals(params[0])) {
                                if (setSocket(device)) {
                                    String rooms = null;
                                    for (int i = 100; i < RESPONSE_TIMEOUT; i += 100) {
                                        rooms = getRooms();
                                        if (rooms != null) {
                                            break;
                                        } else {
                                            try {
                                                Thread.sleep(100);
                                            } catch (Exception e) {
                                            }
                                        }
                                    }
                                    if (rooms != null)
                                        return rooms;
                                } else {
                                    return null;
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        dialog.dismiss();
                        if (o != null) {
                            String result = (String) o;
                            startMainActivity(result);
                        } else {
                            showAlertDialog(BTConnectActivity.this, "Connection failed",
                                    "Make sure you have chosen right device and that its bluetooth is turned on");
                        }
                        super.onPostExecute(o);
                    }
                };
                task.execute(((Button) view).getText());
            }
        };
    }

    public static void set(final Room room, ColorActivity c) {
        AsyncTask task = new AsyncTask() {

            @Override
            protected void onCancelled() {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    OutputStream os = socket.getOutputStream();
                    os.write(room.toJson().toString().getBytes());
                    String result = null;
                    for (int i = 100; i < RESPONSE_TIMEOUT; i += 100) {
                        result = getRooms();
                        if (result != null) {
                            break;
                        } else {
                            Thread.sleep(100);
                        }
                    }
                    if (result == null || !result.trim().equals("200")) {
                        return new Object[]{false, params[0]};
                    }
                    return new Object[]{true, params[0]};
                } catch (Exception e) {
                    Log.e("BT", "Error occurred when sending data", e);
                    return new Object[]{false, params[0]};
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                Object[] ob = (Object[]) o;
                boolean result = (boolean) ob[0];
                ColorActivity c = (ColorActivity) ob[1];
                c.setButtonSendEnabled(true);
                if (!result) {
                    c.showAlertDialog(c, "Bluetooth connection",
                        "Unable to send request. Check your raspberrypi and bluetooth connection");
                }
                super.onPostExecute(o);
            }
        };
        task.execute(c);
    }

    private static String getRooms() {
        try {
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[1024];
            if (is.available() != 0) {
                is.read(buffer);
                String s = new String(buffer, "UTF-8");
                return s;
            }
            return null;
        } catch (Exception e) {
            Log.e("BT", "Error occurred when receiving data", e);
        }
        return null;
    }


    private void startMainActivity(String json) {
        Intent intent = new Intent(this, ChoiceActivity.class);
        ChoiceActivity.mode = ChoiceActivity.BT_MODE;
        intent.putExtra("JSON", json);
        startActivity(intent);
    }


    private boolean setSocket(BluetoothDevice device) {
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
            return false;
        }
        socket = tmp;
        return true;
    }

    public static void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }
    }

}
