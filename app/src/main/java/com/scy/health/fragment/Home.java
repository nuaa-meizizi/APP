package com.scy.health.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.scy.health.R;
import com.scy.health.util.LineChartManager;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Home extends Fragment {

    private LineChart mLineChart;
    private LinearLayout linearLayout;
    private LinearLayout calendar;
    private LinearLayout chart;
    private LinearLayout future_wather;
    private RelativeLayout des;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        initChartView(view);
        //根据屏幕大小隐藏某些部件
        dynamicChange(view);

        return view;
    }

    public void initChartView(View view){

        mLineChart = (LineChart) view.findViewById(R.id.lineChart);
        final LineChartManager lineChartManager1 = new LineChartManager(mLineChart);
        //设置x轴的数据
        final  ArrayList<Float> xValues = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            xValues.add((float) i);
        }
        //设置y轴的数据()
        final List<List<Float>> yValues = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Float> yValue = new ArrayList<>();
            for (int j = 0; j <= 10; j++) {
                yValue.add((float) (Math.random() * 80));
            }
            yValues.add(yValue);
        }
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
        lineChartManager1.setYAxis(100, 0, 11);
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

    public void dynamicChange(View view){
        linearLayout = (LinearLayout)view.findViewById(R.id.linearLayout);
        calendar = (LinearLayout)view.findViewById(R.id.calendar);
        future_wather = (LinearLayout)view.findViewById(R.id.future);
        chart = (LinearLayout)view.findViewById(R.id.chart);
        des = (RelativeLayout)view.findViewById(R.id.des);
        if (!isPad(getContext())){
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            calendar.setVisibility(View.GONE);
            future_wather.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            des.setLayoutParams(lp);
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,0.8f);
            lp.bottomMargin = 30;
            lp.topMargin = 30;
            lp.leftMargin = 30;
            chart.setLayoutParams(lp);
        }
    }


    /**
     * 判断当前设备是手机还是平板
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        // 屏幕宽度
        float screenWidth = display.getWidth();
        // 屏幕高度
        float screenHeight = display.getHeight();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        // 屏幕尺寸
        double screenInches = Math.sqrt(x + y);
        // 大于6尺寸则为Pad
        if (screenInches >= 6.0) {
            return true;
        }
        return false;
    }
}
