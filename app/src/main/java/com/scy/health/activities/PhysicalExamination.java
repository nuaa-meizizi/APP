package com.scy.health.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.scy.health.AsyncTasks.GetBlueToothDataTask;
import com.scy.health.R;
import com.scy.health.util.XfyunASR;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PhysicalExamination extends AppCompatActivity {
    private static final String TAG = "PhysicalExamination";
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
        Log.i(TAG, "onDestroy: 销毁");
        if (GetBlueToothDataTask != null && GetBlueToothDataTask.getStatus()== AsyncTask.Status.RUNNING && !GetBlueToothDataTask.isCancelled())
        {
            GetBlueToothDataTask.cancel(true);
            GetBlueToothDataTask = null;
        }
        super.onDestroy();
    }
    
}
