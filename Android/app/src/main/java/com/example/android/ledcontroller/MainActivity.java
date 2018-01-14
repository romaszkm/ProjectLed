package com.example.android.ledcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    public static int REST_MODE = 0;
    public static int BT_MODE = 1;
    public static int mode;

    private Room[] rooms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getJsonData();
        setButtons();
    }

    @Override
    public void onBackPressed() {
        if (mode == BT_MODE)
            BTConnectActivity.closeSocket();
        super.onBackPressed();
    }

    private void setButtons() {
        int[] buttonIds = {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5};

        for (int i = 0; i < rooms.length; i++) {
            Button button = (Button) findViewById(buttonIds[i]);
            button.setText(rooms[i].getName());
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startColorActivity((Button) v);
                }
            });
        }
    }

    private void startColorActivity(Button v) {
        String name = v.getText().toString();
        Room room = rooms[getRoomId(name)];
        Intent intent = new Intent(this, ColorActivity.class);
        intent.putExtra("REST", true);
        intent.putExtra("name", room.getName());
        intent.putExtra("R", room.getR());
        intent.putExtra("G", room.getG());
        intent.putExtra("B", room.getB());
        intent.putExtra("effect", room.getEffect());

        if (mode == REST_MODE) {
            intent.putExtra("URI", getIntent().getStringExtra("URI"));
        }

        startActivity(intent);
    }

    private int getRoomId(String name) {
        for (int i = 0; i < rooms.length; i++) {
            if (rooms[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void getJsonData() {
        Intent intent = getIntent();
        String json = intent.getStringExtra("JSON");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonRooms = jsonObject.getJSONArray("rooms");
            this.rooms = new Room[jsonRooms.length()];
            for (int i = 0; i < jsonRooms.length(); i++) {
                JSONObject curRoom = jsonRooms.getJSONObject(i);
                String name = curRoom.getString("name");
                int R = curRoom.getInt("R");
                int G = curRoom.getInt("G");
                int B = curRoom.getInt("B");
                String effect = curRoom.getString("effect");
                rooms[i] = new Room(name, R, G, B, effect);
            }
        } catch (Exception e) {
            Log.e("JSON", "Can't read JSON data from server");
        }
    }


}
