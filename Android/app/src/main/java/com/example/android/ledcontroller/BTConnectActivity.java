package com.example.android.ledcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * Created by michalromaszko on 14.01.2018.
 */

public class BTConnectActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private final static String UUID = "f4eee4c3-8d72-479d-b6ec-41e960ad0967";
    private static BluetoothSocket socket;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        enableBT();
    }

    private void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showAlertDialog("Bluetooth not available", "This device does not support bluetooth. Maybe choose internet connection?");
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
            b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(b);
        }
    }

    private void showConnectDialog() {
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Connecting...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private View.OnClickListener getChoiceListener(final Set<BluetoothDevice> pairedDevices) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    showAlertDialog("Bluetooth error", "Please re-enable bluetooth");
                    return;
                }
                showConnectDialog();
                final View view = v;
                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().equals(params[0])) {
                                if (setSocket(device)) {
                                    dialog.dismiss();
                                    String rooms = getRooms();
                                    if (rooms != null)
                                        return rooms;
                                } else {
                                    dialog.dismiss();
                                    return null;
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        String result = (String) o;
                        if (result != null) {
                            startMainActivity(result);
                        } else {
                            showAlertDialog("Connection failed", "Make sure you have chosen right device and that its bluetooth is turned on");
                        }
                        super.onPostExecute(o);
                    }
                };
                task.execute(((Button) view).getText());
            }
        };
    }

    public static Boolean set(Room room) {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(room.toJson().toString().getBytes());
            return true;
        } catch (IOException e) {
            Log.e("BT", "Error occurred when sending data", e);
            return false;
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


    private void startMainActivity(String json) {
        Intent intent = new Intent(this, MainActivity.class);
        MainActivity.mode = MainActivity.BT_MODE;
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
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
