package com.scy.health.AsyncTasks;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.scy.health.R;
import com.scy.health.util.LineChartManager;

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.scy.health.util.SharedPreferencesDataBase.selectAll;

//从本地读取历史数据
public class LocalReadDataTask extends AsyncTask <String, Void, JSONObject>{
    private static final String TAG = "localReadDataTask";
    private LineChart mLineChart;
    private View view;
    private Context context;

    public LocalReadDataTask(Context context,LineChart mLineChart, View view)
    {
        this.mLineChart = mLineChart;
        this.view = view;
        this.context = context;

    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        return selectAll(context,5);
    }

    @Override
    protected void onPostExecute(JSONObject result){
        Log.i(TAG, "onPostExecute: "+result );

        final LineChartManager lineChartManager1 = new LineChartManager(mLineChart);
        //设置x轴的数据
        final ArrayList<Float> xValues = new ArrayList<>();
        final List<List<Float>> yValues = new ArrayList<>();
        TextView latest_date = (TextView)view.findViewById(R.id.latest_date);

        try {
            JSONArray records = result.getJSONArray("data");
            if (records.length() == 0) {
                latest_date.setText("最近更新：无");
                return;
            }
            String format = "MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String date = sdf.format(new Date((Long)records.getJSONObject(records.length()-1).get("time")));
            latest_date.setText("最近更新："+date);
            for (int i = 0; i < records.length(); i++) {
                xValues.add((float) i);
            }

            List<Float> yValue_heartbeat = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                yValue_heartbeat.add((float)records.getJSONObject(i).getInt("heartbeat"));
            }
            yValues.add(yValue_heartbeat);

            List<Float> yValue_temperature = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                yValue_temperature.add((float) records.getJSONObject(i).getDouble("temperature"));
            }
            yValues.add(yValue_temperature);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final List<Integer> colours = new ArrayList<>();
        colours.add(Color.GREEN);
        colours.add(Color.BLUE);
        colours.add(Color.RED);
        colours.add(Color.CYAN);
        colours.add(Color.YELLOW);
        //线的名字集合
        final List<String> names = new ArrayList<>();
        names.add("心率");
        names.add("体温");

        lineChartManager1.showLineChart(xValues, yValues, names, colours);
        lineChartManager1.setDescription("指标趋势图");
        NiceSpinner niceSpinner = (NiceSpinner) view.findViewById(R.id.nice_spinner);
        LinkedList<String> data=new LinkedList<>(Arrays.asList("所有指标","心率","体温"));
        niceSpinner.attachDataSource(data);
        niceSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    lineChartManager1.showLineChart(xValues, yValues, names, colours);
                    Log.i(TAG, "onItemClick: "+yValues);
                }else
                    lineChartManager1.showLineChart(xValues, yValues.get(i-1), names.get(i-1), colours.get(i-1));
            }
        } );
    }
}
