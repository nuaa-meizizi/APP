package com.scy.health.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;
import com.scy.health.Interface.DataBroadcastInterface;
import com.scy.health.R;
import com.scy.health.util.DataBroadcast;
import com.scy.health.util.LineChartManager;
import com.scy.health.util.PremissionDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.LinkedList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.functions.Consumer;

public class Driving extends Fragment implements DataBroadcastInterface {
    private ImageView backup;
    private BottomNavigationView meau;
    private SweetAlertDialog dialog;
    private SharedPreferences sharedPreferences;
    private String tag = "Driving";
    private LineChartManager heart_beat,blood_pressure,temperature;
    private TextView status;
    private DataBroadcast dataBroadcast;
    private static final String TAG = "DrivingFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        final View view =  inflater.inflate(R.layout.fragment_driving, container, false);
        initView(view);
        getPermission();
        dataBroadcast = new DataBroadcast(getContext(),this);       //监听数据变化
        return view;
    }

    public void initView(View view){
        backup = (ImageView)getActivity().findViewById(R.id.backup);
        meau = (BottomNavigationView)getActivity().findViewById(R.id.bottomview);
        sharedPreferences = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        LineChart heart_beat_lc = (LineChart)view.findViewById(R.id.heart_beat);
        LineChart blood_pressure_lc = (LineChart)view.findViewById(R.id.blood_pressure);
        LineChart temperature_lc = (LineChart)view.findViewById(R.id.temperature);

        heart_beat = new LineChartManager(heart_beat_lc,"心率",Color.GREEN);
        blood_pressure = new LineChartManager(blood_pressure_lc,"血压",Color.GREEN);
        temperature = new LineChartManager(temperature_lc,"体温",Color.GREEN);

        status = (TextView)getActivity().findViewById(R.id.status);
        meau.setVisibility(View.GONE);
        backup.setVisibility(View.VISIBLE);
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new SweetAlertDialog(getContext(),SweetAlertDialog.WARNING_TYPE);
                dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                dialog.setContentText("确认退出驾驶模式吗？");
                dialog.setCancelText("点错了");
                dialog.setConfirmText("退出");
                dialog.showCancelButton(true);
                dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        dialog.cancel();
                    }
                });
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        dialog.cancel();
                        meau.setVisibility(View.VISIBLE);
                        backup.setVisibility(View.GONE);
                        meau.selectTab(0);
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        meau.setVisibility(View.VISIBLE);
        backup.setVisibility(View.GONE);
        dataBroadcast.destroy();
    }

    public void callPhone() {
        if (PremissionDialog.lacksPermission("android.permission.CALL_PHONE",getContext())){
            Log.e(tag,"没有电话权限");
            getPermission();
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_CALL);
            Uri data = Uri.parse("tel:" + sharedPreferences.getString("phone","10086"));
            intent.setData(data);
            startActivity(intent);
        }
    }

    private void getPermission(){
        RxPermissions rxPermissions = new RxPermissions(getActivity()); // where this is an Activity instance
        rxPermissions.request(Manifest.permission.CALL_PHONE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) { // 在android 6.0之前会默认返回true
                        } else {
                            PremissionDialog.showMissingPermissionDialog(getContext(),getString(R.string.LACK_CALL_PHONE));
                        }
                    }
                });
    }

    @Override
    public void onaTemperatureChanged(float temperature) {
        Log.i(TAG, "onaTemperatureChanged: "+temperature);
        this.temperature.addEntry(temperature);
    }

    @Override
    public void onHeartbeatChanged(int heartbeat) {
        Log.i(TAG, "onHeartbeatChanged: "+heartbeat);
        this.heart_beat.addEntry(heartbeat);
    }

    @Override
    public void onBpChanged(int bp) {
        Log.i(TAG, "onBpChanged: "+bp);
        this.blood_pressure.addEntry(bp);
    }

    @Override
    public void onChanged(float temperature, int heartbeat, int bp) {

    }
}
