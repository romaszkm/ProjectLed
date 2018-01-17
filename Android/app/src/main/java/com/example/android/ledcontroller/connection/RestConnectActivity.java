package com.example.android.ledcontroller.connection;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.ledcontroller.ChoiceActivity;
import com.example.android.ledcontroller.ColorActivity;
import com.example.android.ledcontroller.misc.MyAbstractActivity;
import com.example.android.ledcontroller.R;
import com.example.android.ledcontroller.misc.Room;

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

public class RestConnectActivity extends MyAbstractActivity {

    //Cache
    private static final String FILENAME = "saved_addresses.cache";
    private List<String> cachedAddresses;

    public final static int RESPONSE_TIMEOUT = 2000;

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
                getRooms(uri);
            }
        } else {
            showAlertDialog(this, "Empty address", "Please fill out the address");
        }
    }

    private void getRooms(final String uri) {
        showConnectDialog(this);
        AsyncTask task = new AsyncTask() {
            HttpURLConnection myConnection;

            @Override
            protected void onCancelled() {
                myConnection.disconnect();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    myConnection = createConnection(uri);
                    if (myConnection != null) {
                        saveAddress(uri);
                        return getJsonFromResponse(myConnection);
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    Log.e("REST", "Cant connect to address: " + uri);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                dialog.dismiss();
                if (o != null) {
                    String result = (String) o;
                    startMainActivity(result, uri);
                } else {
                    showAlertDialog(RestConnectActivity.this, "Connection failed",
                            "Please make sure the address provided is correct and that your connections on phone and raspberry are correct");
                }
                super.onPostExecute(o);
            }
        };
        task.execute();
    }

    private String getJsonFromResponse(HttpURLConnection connection) throws Exception {
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
        myConnection.setReadTimeout(RESPONSE_TIMEOUT);
        myConnection.setConnectTimeout(RESPONSE_TIMEOUT);
        if (myConnection.getResponseCode() == 200) {
            myConnection.disconnect();
            return myConnection;
        } else {
            myConnection.disconnect();
            return null;
        }
    }

    private void startMainActivity(String jsonString, String uri) {
        Intent intent = new Intent(this, ChoiceActivity.class);
        ChoiceActivity.mode = ChoiceActivity.REST_MODE;
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

    public static void set(final Room room, final String uri, ColorActivity c) {
        AsyncTask task = new AsyncTask() {
            HttpURLConnection myConnection;

            @Override
            protected void onCancelled() {
                myConnection.disconnect();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                myConnection = null;
                try {
                    String endpoint = uri.charAt(uri.length() - 1) == '/' ? uri : uri + "/";
                    URL address = new URL(endpoint + "rooms/set");

                    myConnection = (HttpURLConnection) address.openConnection();
                    myConnection.setReadTimeout(RESPONSE_TIMEOUT);
                    myConnection.setConnectTimeout(RESPONSE_TIMEOUT);
                    myConnection.setRequestMethod("POST");
                    myConnection.setDoOutput(true);
                    myConnection.setDoInput(true);
                    DataOutputStream printout;
                    printout = new DataOutputStream(myConnection.getOutputStream());
                    printout.writeBytes(URLEncoder.encode(room.toJson().toString(), "UTF-8"));
                    printout.flush();
                    printout.close();
                    if (myConnection.getResponseCode() == 200) {
                        myConnection.disconnect();
                        return new Object[]{true, params[0]};
                    } else {
                        myConnection.disconnect();
                        Log.i("REST", String.valueOf(myConnection.getResponseCode()));
                        return new Object[]{false, params[0]};
                    }
                } catch (Exception e) {
                    myConnection.disconnect();
                    Log.e("REST", "Can't send request " + e.getMessage());
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
                    c.showAlertDialog(c, "Internet connection",
                        "Unable to send request. Check your raspberrypi and internet connection");
                }
                super.onPostExecute(o);
            }
        };
        task.execute(c);
    }

}
