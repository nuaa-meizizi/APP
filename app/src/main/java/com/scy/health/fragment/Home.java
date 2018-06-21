package com.scy.health.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.scy.health.AsyncTasks.localReadDataTask;
import com.scy.health.R;
import com.scy.health.activities.physicalExamination;

import java.util.List;

public class Home extends Fragment {
    private Context context;
    private LineChart mLineChart;
    private LinearLayout linearLayout,calendar,chart,future_wather;
    private RelativeLayout des;
    private LocationManager locationManager;
    private String locationProvider;
    private Button start;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        //初始化chartview
        context = getContext();
        initView(view);
        //根据屏幕大小隐藏某些部件
        dynamicChange(view);
        getLocation();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationManager != null) {
            // 关闭程序时将监听器移除
            locationManager.removeUpdates(locationListener);
        }
    }

    public void initView(View view) {
        mLineChart = (LineChart) view.findViewById(R.id.lineChart);
        new localReadDataTask(context,mLineChart,view).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        start = (Button)view.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), physicalExamination.class);
                startActivity(intent);
            }
        });
    }

    public void dynamicChange(View view) {
        linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout);
        calendar = (LinearLayout) view.findViewById(R.id.calendar);
        future_wather = (LinearLayout) view.findViewById(R.id.future);
        chart = (LinearLayout) view.findViewById(R.id.chart);
        des = (RelativeLayout) view.findViewById(R.id.des);
        if (!isPad(getContext())) {
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            calendar.setVisibility(View.GONE);
            future_wather.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            des.setLayoutParams(lp);
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f);
            lp.bottomMargin = 30;
            lp.topMargin = 30;
            lp.leftMargin = 30;
            chart.setLayoutParams(lp);
        }
    }

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

    public void getLocation() {
        //1.获取位置管理器
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        //2.获取位置提供器，GPS或是NetWork
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是网络定位
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是GPS定位
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(getContext(), "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println(locationProvider);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            showLocation(location);
        } else {
            // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
        }
    }

    public void showLocation(Location location) {
        String address = location.getLongitude() + "," + location.getLatitude();
        Toast.makeText(getContext(), address, Toast.LENGTH_SHORT).show();
        System.out.println(address);
    }

    LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }
    };
}
