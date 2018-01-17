package com.example.android.ledcontroller.misc;

import org.json.JSONObject;

/**
 * Created by michalromaszko on 13.12.2017.
 */

public class Room {
    private final String name;
    private int R;
    private int G;
    private int B;
    private String effect;

    public Room(String name, int r, int g, int b, String effect) {
        this.name = name;
        R = r;
        G = g;
        B = b;
        this.effect = effect;
    }

    public String getName() {
        return name;
    }

    public int getR() {
        return R;
    }

    public void setR(int r) {
        R = r;
    }

    public int getG() {
        return G;
    }

    public void setG(int g) {
        G = g;
    }

    public int getB() {
        return B;
    }

    public void setB(int b) {
        B = b;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

//    public String toJson() {
//        String json =
//                "'{\"name\": \"" + name + "\"" +
//                        "'{\"R\": " + R +
//                        "'{\"G\": " + G +
//                        "'{\"B\": " + B +
//                        "'{\"effect\": " + effect +
//                        "\"}'";
//        return json;
//    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("R", R);
            json.put("G", G);
            json.put("B", B);
            if (effect != null) {
                json.put("effect", effect);
            }
        } catch (Exception e) {
        }
        return json;
    }
}
