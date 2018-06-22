package com.scy.health.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferencesDataBase {
    private static final String TAG = "SpDataBase";

    public static JSONObject selectAll(Context context,int N){
        JSONArray recortds = new JSONArray();
        SharedPreferences sp = context.getSharedPreferences("health", Context.MODE_PRIVATE);
        Set<String> ids = new HashSet<String>(sp.getStringSet("records", new HashSet<String>()));
        List<String> idsList = new ArrayList<>(ids);
        Collections.sort(idsList);
        int start = idsList.size()-N > 0?idsList.size()-N:0;
        idsList = idsList.subList(start,idsList.size());
        try {
            for (String id : idsList) {
                JSONObject tmp = new JSONObject();
                long time = sp.getLong("time_"+id,0);
                float temperature = sp.getFloat("temperature_"+id,0);
                int heartbeat = sp.getInt("heartbeat_"+id,0);
                tmp.put("time",time);
                tmp.put("temperature",temperature);
                tmp.put("heartbeat",heartbeat);
                recortds.put(tmp);
            }
            return new JSONObject().put("data",recortds).put("status",1);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean insert(Context context,float temperature,int heartbeat){
        SharedPreferences sp = context.getSharedPreferences("health", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Set<String> records = new HashSet<String>(sp.getStringSet("records", new HashSet<String>()));
        String id = Long.toString(System.currentTimeMillis());
        records.add(id);
        editor.putStringSet("records",records);
        editor.putLong("time_"+id,System.currentTimeMillis());
        editor.putFloat("temperature_"+id,temperature);
        editor.putInt("heartbeat_"+id,heartbeat);
        editor.apply();
        return true;
    }
}
