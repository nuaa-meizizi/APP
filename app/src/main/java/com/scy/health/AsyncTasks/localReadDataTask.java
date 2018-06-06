package com.scy.health.AsyncTasks;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;

import com.github.mikephil.charting.charts.LineChart;
import com.scy.health.R;
import com.scy.health.util.LineChartManager;

import org.angmarch.views.NiceSpinner;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.scy.health.dataInterface.senserData.physicalExamination;

//从本地读取历史数据
public class localReadDataTask extends AsyncTask <String, Void, JSONObject>{
    private LineChart mLineChart;
    private View view;

    public localReadDataTask(LineChart mLineChart,View view)
    {
        this.mLineChart = mLineChart;
        this.view = view;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        return physicalExamination();
    }

    @Override
    protected void onPostExecute(JSONObject result){
        final LineChartManager lineChartManager1 = new LineChartManager(mLineChart);
        //设置x轴的数据
        final ArrayList<Float> xValues = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            xValues.add((float) i);
        }
        //设置y轴的数据()
        final List<List<Float>> yValues = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<Float> yValue = new ArrayList<>();
            for (int j = 0; j <= 10; j++) {
                yValue.add((float) (Math.random() * 80));
            }
            yValues.add(yValue);
        }
        List<Float> yValue = new ArrayList<>();
        for (int j = 0; j <= 10; j++) {
            yValue.add((float) (Math.random() * 1000));
        }
        yValues.add(yValue);
        final List<Integer> colours = new ArrayList<>();
        colours.add(Color.GREEN);
        colours.add(Color.BLUE);
        colours.add(Color.RED);
        colours.add(Color.CYAN);
        colours.add(Color.YELLOW);
        //线的名字集合
        final List<String> names = new ArrayList<>();
        names.add("体温");
        names.add("体重");
        names.add("心跳");
        names.add("血压");
        names.add("血脂");

        lineChartManager1.showLineChart(xValues, yValues, names, colours);
       //lineChartManager1.setYAxis(100, 0, 11);
        lineChartManager1.setDescription("指标趋势图");


        NiceSpinner niceSpinner = (NiceSpinner) view.findViewById(R.id.nice_spinner);
        LinkedList<String> data=new LinkedList<>(Arrays.asList("所有指标","体温", "体重", "心跳", "血压","血脂"));
        niceSpinner.attachDataSource(data);
        niceSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0)
                    lineChartManager1.showLineChart(xValues, yValues, names, colours);
                else
                    lineChartManager1.showLineChart(xValues, yValues.get(i-1), names.get(i-1), colours.get(i-1));
            }
        } );
    }
}
