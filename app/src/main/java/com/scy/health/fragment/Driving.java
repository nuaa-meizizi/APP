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

import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;
import com.scy.health.R;
import com.scy.health.util.PremissionDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.functions.Consumer;

public class Driving extends Fragment {
    private ImageView backup;
    private BottomNavigationView meau;
    private SweetAlertDialog dialog;
    private SharedPreferences sharedPreferences;
    private String tag = "Driving";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_driving, container, false);
        initView(view);
        getPermission();
        return view;
    }

    public void initView(View view){
        backup = (ImageView)getActivity().findViewById(R.id.backup);
        meau = (BottomNavigationView)getActivity().findViewById(R.id.bottomview);
        sharedPreferences = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);

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


}
