package com.example.android.ledcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static android.R.attr.data;
import static android.R.id.list;

public class RestConnectActivity extends AppCompatActivity {

    private String FILENAME = "saved_addresses.cache";
    private List<String> cachedAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_connect);
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
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String endpoint = uri.charAt(uri.length() - 1) == '/' ? uri : uri + "/";
                            endpoint += "rooms/get";
                            URL address = new URL(endpoint);
                            HttpURLConnection myConnection =
                                    (HttpURLConnection) address.openConnection();
                            if (myConnection.getResponseCode() == 200) {
                                myConnection.disconnect();
                                saveAddress(uri);
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                                StringBuilder stringBuilder = new StringBuilder();
                                String line;
                                while ((line = bufferedReader.readLine()) != null) {
                                    stringBuilder.append(line).append('\n');
                                }
                                startMainActivity(stringBuilder.toString(), uri);
                            } else {
                                myConnection.disconnect();
                            }
                        } catch (Exception e) {
                            Log.e("REST", "Cant connect to address: " + uri);
                        }
                    }
                });
            }
        } else {
            //TODO informacja ze trzeba wypelnic
        }
    }

    private void startMainActivity(String jsonString, String uri) {
        Intent intent = new Intent(this, MainActivity.class);
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
}
