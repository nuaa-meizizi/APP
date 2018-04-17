package com.scy.health.fragment;

import android.graphics.Color;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_driving, container, false);
        initView(view);

        return view;
    }

    public void initView(View view){
        backup = (ImageView)getActivity().findViewById(R.id.backup);
        meau = (BottomNavigationView)getActivity().findViewById(R.id.bottomview);

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
}
