package com.example.android.ledcontroller;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class ColorActivity extends AppCompatActivity {

    private final static String EFFECT_SWIPE = "swipe";
    private final static String EFFECT_BREATHING = "breathing";
    private final static String ENDPOINT_REST_SET = "rooms/set";

    private String effect;
    private SeekBar bar_R;
    private SeekBar bar_G;
    private SeekBar bar_B;
    private CheckBox swipe;
    private CheckBox breathing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

        setCurrentState();

    }

    private void setCurrentState() {
        Intent intent = getIntent();
        int r = intent.getIntExtra("R", 0);
        int g = intent.getIntExtra("G", 0);
        int b = intent.getIntExtra("B", 0);
        effect = intent.getStringExtra("effect");
        if (effect.toLowerCase().trim().equals("null")) {
            effect = null;
        }
        bar_R = (SeekBar) findViewById(R.id.valueR);
        bar_G = (SeekBar) findViewById(R.id.valueG);
        bar_B = (SeekBar) findViewById(R.id.valueB);
        swipe = (CheckBox) findViewById(R.id.effectSwipe);
        breathing = (CheckBox) findViewById(R.id.effectBreathing);

        bar_R.setProgress(r);
        bar_G.setProgress(g);
        bar_B.setProgress(b);

        if (effect != null) {
            swipe.setSelected(effect.equals(EFFECT_SWIPE));
            swipe.setSelected(effect.equals(EFFECT_BREATHING));
        }
    }

    public void set(View v) {
        final Room room = new Room(getIntent().getStringExtra("name"), bar_R.getProgress(), bar_G.getProgress(), bar_B.getProgress(), effect);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (MainActivity.mode == MainActivity.REST_MODE) {
                        String uri = getIntent().getStringExtra("URI");
                        String endpoint = uri.charAt(uri.length() - 1) == '/' ? uri : uri + "/";
                        URL address = new URL(endpoint + ENDPOINT_REST_SET);

                        HttpURLConnection myConnection
                                = (HttpURLConnection) address.openConnection();
                        myConnection.setRequestMethod("POST");
                        myConnection.setDoOutput(true);
                        myConnection.setDoInput(true);
                        DataOutputStream printout;
                        printout = new DataOutputStream(myConnection.getOutputStream());
                        printout.writeBytes(URLEncoder.encode(room.toJson().toString(), "UTF-8"));
                        printout.flush();
                        printout.close();

                        Log.e("NET", "" + myConnection.getResponseCode());
                        Log.e("NET", myConnection.getResponseMessage());
                    }
                } catch (Exception e) {
                    Log.e("REST", "Can't send request");
                }
            }
        });
    }

    public void actionSwipeCheckBox(View v) {
        if (swipe.isChecked()) {
            breathing.setChecked(false);
        }
        if (swipe.isChecked()) {
            effect = EFFECT_SWIPE;
        } else if (breathing.isChecked()) {
            effect = EFFECT_BREATHING;
        } else {
            effect = null;
        }
    }

    public void actionBreathingCheckBox(View v) {
        if (breathing.isChecked()) {
            swipe.setChecked(false);
        }
        if (swipe.isChecked()) {
            effect = EFFECT_SWIPE;
        } else if (breathing.isChecked()) {
            effect = EFFECT_BREATHING;
        } else {
            effect = null;
        }
    }
}
