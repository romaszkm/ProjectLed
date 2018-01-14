package com.example.android.ledcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }

    public void actionRest(View v) {
        Intent intent = new Intent(this, RestConnectActivity.class);
        startActivity(intent);
    }

    public void actionBT(View v) {
        Intent intent = new Intent(this, BTConnectActivity.class);
        startActivity(intent);
    }

}
