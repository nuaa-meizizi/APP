package com.scy.health.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.scy.health.AsyncTasks.PhysicalExaminationTask;
import com.scy.health.R;
import com.scy.health.util.XfyunASR;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PhysicalExamination extends AppCompatActivity {
    private static final String TAG = "PhysicalExaminationTask";
    private Context context;
    private XfyunASR xfyunASR;
    private PhysicalExaminationTask PhysicalExaminationTask;
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
        PhysicalExaminationTask = new PhysicalExaminationTask(context,sweetAlertDialog,xfyunASR);
        PhysicalExaminationTask.execute();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: 销毁");
        if (PhysicalExaminationTask != null && PhysicalExaminationTask.getStatus()== AsyncTask.Status.RUNNING && !PhysicalExaminationTask.isCancelled())
        {
            PhysicalExaminationTask.cancel(true);
            PhysicalExaminationTask = null;
        }
        super.onDestroy();
    }
    
}
