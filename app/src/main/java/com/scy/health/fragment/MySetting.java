package com.scy.health.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.scy.health.R;
import com.scy.health.activities.LoginActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MySetting extends Fragment {
    private TableRow trsex,contacts;
    private ImageView sprogress;
    private ProgressBar progress;
    private TextView phone,sex,clear;
    private SharedPreferences sharedPreferences;
    private Editor editor;
    String single[] = {"男","女"};
    private SweetAlertDialog dialog;
    String singleChoice;
    private Switch radioOn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_setting, container, false);
        initView(view);

        return view;

    }

    public void initView(View view){
        trsex = (TableRow)view.findViewById(R.id.trsex);
        clear = (TextView)view.findViewById(R.id.trclear);
        contacts = (TableRow)view.findViewById(R.id.contacts);
        phone = (TextView)view.findViewById(R.id.phone);
        sprogress = (ImageView)view.findViewById(R.id.sprogress);
        progress = (ProgressBar)view.findViewById(R.id.progress);
        sex = (TextView)view.findViewById(R.id.sex);
        radioOn = (Switch)view.findViewById(R.id.radioSwitch);
        sharedPreferences = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        phone.setText(sharedPreferences.getString("phone","10086"));
        sex.setText(sharedPreferences.getString("sex","男"));
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getContext().getSharedPreferences("health", Context.MODE_PRIVATE);
                Editor editor = sp.edit();
                editor.clear().apply();
                Toast.makeText(getContext(),"清除成功",Toast.LENGTH_SHORT).show();
            }
        });

        sprogress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = sharedPreferences.getString("name",null);
                String password = sharedPreferences.getString("password",null);
                if (name != null && password != null)
                {
                    sprogress.setVisibility(View.GONE);
                    progress.setVisibility(View.VISIBLE);
                    //登陆 同步
                }
                else {  //需要登陆
                    dialog = new SweetAlertDialog(getContext(),SweetAlertDialog.NORMAL_TYPE);
                    dialog.setContentText("同步功能需要登陆");
                    dialog.setCancelText("算了");
                    dialog.setConfirmText("现在登陆");
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
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                    dialog.show();
                }
            }
        });

        trsex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.AlertDialog.Builder builder;
                builder = new android.app.AlertDialog.Builder(getContext());
                builder.setTitle("修改性别");
                builder.setSingleChoiceItems(single,3, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        singleChoice = single[which];
                    }
                });
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sex.setText(singleChoice);
                        editor.putString("sex",singleChoice);
                        editor.commit();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
            }
        });

        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.AlertDialog.Builder builder;
                final EditText et = new EditText(getContext());
                et.setInputType(InputType.TYPE_CLASS_PHONE);
                et.setText(phone.getText());
                builder = new android.app.AlertDialog.Builder(getContext());
                builder.setTitle("修改紧急联系人");
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        phone.setText(et.getText().toString());
                        editor.putString("phone",et.getText().toString());
                        editor.commit();
                    }
                });
                builder.setView(et);
                builder.setNegativeButton("取消", null);
                builder.create().show();
            }
        });

        radioOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("radio",b).apply();
            }
        });
    }
}
