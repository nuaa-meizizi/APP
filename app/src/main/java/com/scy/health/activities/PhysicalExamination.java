package com.scy.health.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scy.health.AsyncTasks.GetBlueToothDataTask;
import com.scy.health.R;
import com.scy.health.ViewPagerAdapter;
import com.scy.health.util.XfyunASR;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PhysicalExamination extends AppCompatActivity {
    private static final String TAG = "PhysicalExamination";
    private ViewPager viewPager;
    private Context context;
    private XfyunASR xfyunASR;
    private GetBlueToothDataTask GetBlueToothDataTask;
    private SweetAlertDialog sweetAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_examination);
        context = this;
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setContentText("体检中");
        sweetAlertDialog.show();
        sweetAlertDialog.setCancelable(false);
        xfyunASR = new XfyunASR(this);
        getData();
    }

    public void getData(){
        GetBlueToothDataTask = new GetBlueToothDataTask(context,sweetAlertDialog,xfyunASR);
        GetBlueToothDataTask.execute();
    }

    @Override
    public void onDestroy() {
        if (GetBlueToothDataTask != null && GetBlueToothDataTask.getStatus()== AsyncTask.Status.RUNNING && !GetBlueToothDataTask.isCancelled())
        {
            GetBlueToothDataTask.cancel(true);
            GetBlueToothDataTask = null;
        }
        super.onDestroy();
    }
}
