package com.scy.health.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.scy.health.AsyncTasks.localReadDataTask;
import com.scy.health.R;

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

        //初始化chartview
        initChartView(view);
        //根据屏幕大小隐藏某些部件
        dynamicChange(view);
        return view;
    }

    public void initChartView(View view){

        mLineChart = (LineChart) view.findViewById(R.id.lineChart);
        new localReadDataTask(mLineChart,view).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
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
