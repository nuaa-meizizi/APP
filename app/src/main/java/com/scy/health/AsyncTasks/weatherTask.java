package com.scy.health.AsyncTasks;

import android.os.AsyncTask;
import org.json.JSONObject;

//获取天气信息
public class weatherTask extends AsyncTask<String, Void, JSONObject> {
    public void weatherTask(){

    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result){}

}
