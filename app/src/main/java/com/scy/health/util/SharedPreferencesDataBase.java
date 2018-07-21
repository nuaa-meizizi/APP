package com.scy.health.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
                int bp0 = sp.getInt("bp0_"+id,0);
                int bp1 = sp.getInt("bp1_"+id,0);
                int weight = sp.getInt("weight"+id,45);
                int type = sp.getInt("type"+id,0);
                tmp.put("time",time);
                tmp.put("temperature",String.valueOf(temperature));
                tmp.put("heartbeat",heartbeat);
                tmp.put("bp0",bp0);
                tmp.put("bp1",bp1);
                tmp.put("weight",weight);
                tmp.put("type",type);
                recortds.put(tmp);
            }
            return new JSONObject().put("data",recortds).put("status",1);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject selectTemperature(Context context,int N){
        JSONArray temperatures = new JSONArray();
        SharedPreferences sp = context.getSharedPreferences("health", Context.MODE_PRIVATE);
        Set<String> ids = new HashSet<String>(sp.getStringSet("records", new HashSet<String>()));
        List<String> idsList = new ArrayList<>(ids);
        Collections.sort(idsList);
        int start = idsList.size()-N > 0?idsList.size()-N:0;
        idsList = idsList.subList(start,idsList.size());
        try {
            for (String id : idsList) {
                float temperature = sp.getFloat("temperature_"+id,0);
                temperatures.put(temperature);
            }
            return new JSONObject().put("data",temperatures).put("status",1);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject selectHeartBeat(Context context,int N){
        JSONArray heartbeats = new JSONArray();
        SharedPreferences sp = context.getSharedPreferences("health", Context.MODE_PRIVATE);
        Set<String> ids = new HashSet<String>(sp.getStringSet("records", new HashSet<String>()));
        List<String> idsList = new ArrayList<>(ids);
        Collections.sort(idsList);
        int start = idsList.size()-N > 0?idsList.size()-N:0;
        idsList = idsList.subList(start,idsList.size());
        try {
            for (String id : idsList) {
                int temperature = sp.getInt("heartbeat_"+id,0);
                heartbeats.put(temperature);
            }
            return new JSONObject().put("data",heartbeats).put("status",1);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject selectBp(Context context,int N){
        JSONArray bps = new JSONArray();
        SharedPreferences sp = context.getSharedPreferences("health", Context.MODE_PRIVATE);
        Set<String> ids = new HashSet<String>(sp.getStringSet("records", new HashSet<String>()));
        List<String> idsList = new ArrayList<>(ids);
        Collections.sort(idsList);
        int start = idsList.size()-N > 0?idsList.size()-N:0;
        idsList = idsList.subList(start,idsList.size());
        try {
            for (String id : idsList) {
                int bp0 = sp.getInt("bp0_"+id,0);
                int bp1 = sp.getInt("bp1_"+id,0);
                JSONObject bpObjet = new JSONObject();
                bpObjet.put("0",bp0);
                bpObjet.put("1",bp1);
                bps.put(bpObjet);
            }
            return new JSONObject().put("data",bps).put("status",1);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean insert(Context context,float temperature,int heartbeat,int weight,int[] bp,int type){
        SharedPreferences sp = context.getSharedPreferences("health", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Set<String> records = new HashSet<String>(sp.getStringSet("records", new HashSet<String>()));
        String id = Long.toString(System.currentTimeMillis());
        records.add(id);
        editor.putStringSet("records",records);
        editor.putLong("time_"+id,System.currentTimeMillis());
        editor.putFloat("temperature_"+id,temperature);
        editor.putInt("heartbeat_"+id,heartbeat);
        editor.putInt("bp0_"+id,bp[0]);
        editor.putInt("bp1_"+id,bp[1]);
        editor.putInt("type_"+id,type);
        editor.putInt("weight_"+id,weight);
        editor.apply();
        return true;
    }

    public static boolean save(Context context,long time,float temperature,int heartbeat,int weight,int[] bp,int type){
        SharedPreferences sp = context.getSharedPreferences("health", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Set<String> records = new HashSet<String>(sp.getStringSet("records", new HashSet<String>()));
        String id = Long.toString(time);
        records.add(id);
        editor.putStringSet("records",records);
        editor.putLong("time_"+id,time);
        editor.putFloat("temperature_"+id,temperature);
        editor.putInt("heartbeat_"+id,heartbeat);
        editor.putInt("bp0_"+id,bp[0]);
        editor.putInt("bp1_"+id,bp[1]);
        editor.putInt("type_"+id,type);
        editor.putInt("weight_"+id,weight);
        editor.apply();
        return true;
    }

    public static void savaAll(Context context,JSONArray remoteData){
        for (int i = 0; i < remoteData.length(); i++) {
            try {
                JSONObject data = remoteData.getJSONObject(i);
                float temperature = (float) data.getDouble("temperature");
                int heartbeat = data.getInt("heartbeat");
                int[] bp = new int[2];
                long addTime = data.getLong("addTime");
                bp[0] = data.getInt("dPressure");
                bp[1] = data.getInt("sPressure");
                int type = data.getInt("type");
                int weight = data.getInt("weight");
                save(context,addTime,temperature,heartbeat,weight,bp,type);
            } catch (Exception e) {
                Log.e(TAG, "savaAll: ", e);
            }
        }
    }
}
