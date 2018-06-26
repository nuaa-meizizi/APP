package com.scy.health.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.scy.health.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

//获取天气信息
public class WeatherTask extends AsyncTask<String, Void, JSONObject> {
    private String location;
    private Context context;
    private View view;
    private static final String TAG = "WeatherTask";

    public  WeatherTask(String location,Context context,View view){
        this.location = location;
        this.context = context;
        this.view = view;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        String param = "key=dae604e10bf7480b8a4e816be2fa311b&location="+location;
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            //接口地址
            String url = "https://free-api.heweather.com/s6/weather";
            URL uri = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");
            //发送参数
            connection.setDoOutput(true);
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(param);
            out.flush();
            //接收结果
            is = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            //缓冲逐行读取
            while ( ( line = br.readLine() ) != null ) {
                sb.append(line);
            }
            Log.d(TAG, "doInBackground() returned: " + sb.toString());
            return new JSONObject(sb.toString());
        } catch ( Exception ignored ) {
        } finally {
            //关闭流
            try {
                if(is!=null){
                    is.close();
                }
                if(br!=null){
                    br.close();
                }
            } catch ( Exception ignored ) {
                Log.e(TAG, "doInBackground: ",ignored );
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result){
        TextView location = (TextView)view.findViewById(R.id.location);
        TextView update_date = (TextView)view.findViewById(R.id.update_date);
        TextView tmp = (TextView)view.findViewById(R.id.tmp);
        ImageView wpng = (ImageView)view.findViewById(R.id.wpng);
        TextView cond = (TextView)view.findViewById(R.id.cond_txt);
        TextView hum = (TextView)view.findViewById(R.id.hum);
        TextView wind_dir = (TextView)view.findViewById(R.id.wind_dir);
        TextView wind_spd = (TextView)view.findViewById(R.id.wind_spd);
        TextView vis = (TextView)view.findViewById(R.id.vis);
        TextView future_cond = (TextView)view.findViewById(R.id.cond_txt_d);
        TextView future_tmp = (TextView)view.findViewById(R.id.future_tmp);
        TextView future_wind = (TextView)view.findViewById(R.id.future_wind);

        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");//设置日期格式

        try {
            String location_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("basic").getString("location");
            String parent_city_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("basic").getString("parent_city");
            String tmp_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("now").getString("tmp");
            String cond_code = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("now").getString("cond_code");
            String cond_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("now").getString("cond_txt");
            String hum_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("now").getString("hum");
            String wind_dir_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("now").getString("wind_dir");
            String wind_spd_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("now").getString("wind_spd");
            String vis_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONObject("now").getString("vis");
            String future_cond_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONArray("daily_forecast").getJSONObject(0).getString("cond_txt_d");
            String future_tmp_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONArray("daily_forecast").getJSONObject(0).getString("tmp_max");
            String future_wind_text = result.getJSONArray("HeWeather6").getJSONObject(0).getJSONArray("daily_forecast").getJSONObject(0).getString("wind_dir");

            location.setText(parent_city_text+" "+location_text);
            update_date.setText(df.format(new Date()));
            tmp.setText(tmp_text+"°");

            Log.e(TAG, "onPostExecute: "+ update_date.getText().toString().substring(6,8));
            if(Integer.valueOf(update_date.getText().toString().substring(6,8)) > 18) {
                int resId = context.getResources().getIdentifier("w" + cond_code+"n", "drawable", "com.scy.health");
                if (resId <= 0)
                    resId = context.getResources().getIdentifier("w" + cond_code, "drawable", "com.scy.health");
                wpng.setImageDrawable(context.getResources().getDrawable(resId));
            }
            else {
                int resId = context.getResources().getIdentifier("w" + cond_code, "drawable", "com.scy.health");
                wpng.setImageDrawable(context.getResources().getDrawable(resId));
            }
            cond.setText(cond_text);
            hum.setText(hum_text+"%");
            wind_dir.setText(wind_dir_text);
            wind_spd.setText(wind_spd_text+"km/h");
            vis.setText(vis_text+"km");
            future_cond.setText(future_cond_text);
            future_tmp.setText(future_tmp_text+"°");
            future_wind.setText(future_wind_text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
