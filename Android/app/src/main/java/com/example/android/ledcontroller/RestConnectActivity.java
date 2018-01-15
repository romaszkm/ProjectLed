package com.example.android.ledcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

public class RestConnectActivity extends AppCompatActivity {

    private final String FILENAME = "saved_addresses.cache";
    private List<String> cachedAddresses;
    public static int state;

    public static final int STATE_OK = 0;
    public static final int STATE_PROBLEM = 1;
    public static final int STATE_IN_PROGRESS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_connect);

        setCachedAddresses();

    }

    private void setCachedAddresses() {
        cachedAddresses = getAddresses();
        if (cachedAddresses != null) {
            Spinner cachedAddressesSpinner = (Spinner) findViewById(R.id.spinnerCachedAddresses);
            if (cachedAddressesSpinner != null) {
                cachedAddressesSpinner.setAdapter(new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, cachedAddresses));
                cachedAddressesSpinner.setOnItemSelectedListener(new SpinnerActivity());
            }
        }
    }

    public void actionConnect(View v) {
        TextView address = (TextView) findViewById(R.id.editText);
        CharSequence cs = address.getText();
        if (cs != null) {
            final String uri = cs.toString();
            if (!uri.trim().isEmpty()) {
                state = STATE_IN_PROGRESS;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpURLConnection myConnection = createConnection(uri);
                            if (myConnection != null) {
                                saveAddress(uri);
                                state = STATE_OK;
                                startMainActivity(getJson(myConnection), uri);
                            } else {
                                state = STATE_PROBLEM;
                            }
                        } catch (Exception e) {
                            state = STATE_PROBLEM;
                            Log.e("REST", "Cant connect to address: " + uri);
                        }
                    }
                });
                while (state == STATE_IN_PROGRESS) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
                if (state == STATE_PROBLEM)
                    showAlertDialog("Unable to connect", "Please make sure the address provided is correct and that your connections on phone and raspberry are ok");
            }
        } else {
            showAlertDialog("Empty address", "Please fill out the address");
        }
    }

    private String getJson(HttpURLConnection connection) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append('\n');
        }
        return stringBuilder.toString();
    }

    private HttpURLConnection createConnection(String uri) throws Exception {
        String endpoint = uri.charAt(uri.length() - 1) == '/' ? uri : uri + "/";
        endpoint += "rooms/get";
        URL address = new URL(endpoint);
        HttpURLConnection myConnection =
                (HttpURLConnection) address.openConnection();
        if (myConnection.getResponseCode() == 200) {
            myConnection.disconnect();
            return myConnection;
        } else {
            myConnection.disconnect();
            return null;
        }
    }

    private void startMainActivity(String jsonString, String uri) {
        Intent intent = new Intent(this, MainActivity.class);
        MainActivity.mode = MainActivity.REST_MODE;
        intent.putExtra("JSON", jsonString);
        intent.putExtra("URI", uri);
        startActivity(intent);
    }


    private void saveAddress(String uri) {
        try {
            if (cachedAddresses == null) {
                cachedAddresses = new LinkedList<String>();
            }
            cachedAddresses.add(uri);
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(listToString(cachedAddresses).getBytes());
            fos.close();
        } catch (Exception e) {
            Log.i("cache", "Cant open and/or write to file:\n" + e.getMessage());
        }
    }

    private List<String> getAddresses() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            List<String> tmp = new LinkedList<String>();
            while ((line = br.readLine()) != null) {
                tmp.add(line);
            }
            return tmp;
        } catch (Exception e) {
            Log.i("cache", "Cant open and/or read file:\n" + e.getMessage());
            return new LinkedList<>();
        }
    }

    private String listToString(List<String> list) {
        String str = "";
        for (String addr : list) {
            str += addr + "\n";
        }
        return str;
    }

    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            TextView address = (TextView) findViewById(R.id.editText);
            address.setText((String) parent.getItemAtPosition(pos));
        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public static void set(Room room, String uri) {
        final String url = uri;
        final Room Room = room;
        state = STATE_IN_PROGRESS;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String endpoint = url.charAt(url.length() - 1) == '/' ? url : url + "/";
                    URL address = new URL(endpoint + "rooms/set");

                    HttpURLConnection myConnection
                            = (HttpURLConnection) address.openConnection();
                    myConnection.setRequestMethod("POST");
                    myConnection.setDoOutput(true);
                    myConnection.setDoInput(true);
                    DataOutputStream printout;
                    printout = new DataOutputStream(myConnection.getOutputStream());
                    printout.writeBytes(URLEncoder.encode(Room.toJson().toString(), "UTF-8"));
                    printout.flush();
                    printout.close();

                    if (myConnection.getResponseCode() != 200) {
                        state = STATE_PROBLEM;
                        Log.i("REST", "" + myConnection.getResponseCode());
                        Log.i("REST", myConnection.getResponseMessage());

                    } else {
                        state = STATE_OK;
                    }
                } catch (Exception e) {
                    state = STATE_PROBLEM;
                    Log.e("REST", "Can't send request " + e.getMessage());
                }
            }
        });
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

}
