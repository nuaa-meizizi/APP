package com.scy.health.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;
import com.scy.health.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Driving extends Fragment {
    private ImageView backup;
    private BottomNavigationView meau;
    private SweetAlertDialog dialog;
    private SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_driving, container, false);
        initView(view);
        callPhone();
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
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + sharedPreferences.getString("phone","10086"));
        intent.setData(data);
        startActivity(intent);
    }
}
