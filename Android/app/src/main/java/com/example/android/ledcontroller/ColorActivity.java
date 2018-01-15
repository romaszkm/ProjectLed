package com.example.android.ledcontroller;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;

public class ColorActivity extends AppCompatActivity {

    private final static String EFFECT_SWIPE = "swipe";
    private final static String EFFECT_BREATHING = "breathing";

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

        bar_R.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
        bar_G.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY));
        bar_B.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY));

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int r = bar_R.getProgress();
                int g = bar_G.getProgress();
                int b = bar_B.getProgress();

                Button preview = (Button) findViewById(R.id.colorPreview);
                preview.setBackgroundColor(Color.argb(255, r, g, b));
            }
        };

        bar_R.setOnSeekBarChangeListener(listener);
        bar_G.setOnSeekBarChangeListener(listener);
        bar_B.setOnSeekBarChangeListener(listener);

        if (effect != null) {
            swipe.setSelected(effect.equals(EFFECT_SWIPE));
            swipe.setSelected(effect.equals(EFFECT_BREATHING));
        }
    }

    public void set(View v) {
        final Room room = new Room(getIntent().getStringExtra("name"), bar_R.getProgress(), bar_G.getProgress(), bar_B.getProgress(), effect);
        if (MainActivity.mode == MainActivity.REST_MODE) {
            RestConnectActivity.set(room, getIntent().getStringExtra("URI"));
            if (!isSuccess()) {
                showAlertDialog("Internet connection", "Unable to send request. Check your raspberrypi and internet connection");
            }
        } else {
            if (!BTConnectActivity.set(room) || !isSuccess()) {
                showAlertDialog("Bluetooth connection", "Unable to send request. Check your raspberrypi and bluetooth connection");
            }
        }
    }

    private boolean isSuccess() {
        if (MainActivity.mode == MainActivity.REST_MODE) {
            while (RestConnectActivity.state == RestConnectActivity.STATE_IN_PROGRESS) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                if (RestConnectActivity.state == RestConnectActivity.STATE_PROBLEM) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
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

    public void showAlertDialog(String title, String message) {
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
                .setNegativeButton("Go back to connection choice", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ColorActivity.this, IntroActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }
}
