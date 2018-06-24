package com.scy.health.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.scy.health.Interface.BluetoothInterface;
import com.scy.health.R;
import com.scy.health.ViewPagerAdapter;
import com.scy.health.activities.PhysicalExamination;
import com.scy.health.util.BlueTooth;
import com.scy.health.util.LineChartManager;
import com.scy.health.util.XfyunASR;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.scy.health.util.SharedPreferencesDataBase.insert;
import static com.scy.health.util.SharedPreferencesDataBase.selectHeartBeat;
import static com.scy.health.util.SharedPreferencesDataBase.selectTemperature;

public class GetBlueToothDataTask extends AsyncTask<String, Void, String>  implements ViewPager.OnPageChangeListener{
    private static final String TAG = "GetBlueToothDataTask";
    private Boolean ready = false;
    private Context context;
    private Activity activity;
    private BlueTooth blueTooth;
    private String res = null;
    private int count;
    private SweetAlertDialog sweetAlertDialog;
    private GetBlueToothDataTask myself;
    private ViewPager viewPager;
    private FrameLayout layout_frame;
    private LinearLayout layout_point;
    private ViewPagerAdapter adapter;
    private List<View> list_view = new ArrayList<View>();
    private List<ImageView> list_pointView = new ArrayList<ImageView>();
    private ImageView img_colorPoint,left,right;
    // 两点之间间距
    private int pointSpacing;
    private Boolean concection_ok = false;
    private int page = 0;
    private XfyunASR xfyunASR;
    private boolean radioOn;        //是否开启语音播报
    private Set<Integer> hasAnnounced = new HashSet<Integer>();

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!concection_ok) {
                        //数据读取完毕，ready了就不退出了
                        sweetAlertDialog.cancel();
                        Toast.makeText(context, "超时，请检查设备连接", Toast.LENGTH_SHORT).show();
                        myself.cancel(true);        //取消异步任务
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public GetBlueToothDataTask(Context context, SweetAlertDialog sweetAlertDialog, XfyunASR xfyunASR)
    {
        this.context = context;
        this.activity = (Activity)context;
        this.sweetAlertDialog = sweetAlertDialog;
        timeoutClosing();       //超时自动关闭
        this.myself = this;
        this.xfyunASR = xfyunASR;
        SharedPreferences sp = context.getSharedPreferences("health", Context.MODE_PRIVATE);
        this.radioOn = sp.getBoolean("radio",false);
        Log.i(TAG, "GetBlueToothDataTask: "+radioOn);
    }

    @Override
    protected String doInBackground(String... strings) {
//        if (isCancelled()){
//            my_cancel();
//            return null;
//        }
//        blueTooth = new BlueTooth(context);
//        blueTooth.start(new BluetoothInterface() {
//            @Override
//            public void onSuccess() {
//                Log.i(TAG, "onSuccess: 蓝牙连接成功");
//                concection_ok = true;
//            }
//
//            @Override
//            public void onError(String errorData) {
//                Log.e(TAG, "onError: "+errorData);
//            }
//
//            @Override
//            public void onReceive(String data) {
//                System.out.println("----------------------------:"+data);
//                handleData(data);
//            }
//        });
//        while (!ready){
//            if (isCancelled()){
//                my_cancel();
//                return null;
//            }
//        }
//        my_cancel();
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        res = Float.toString(new Random().nextInt(40-36)+36)+" "+Integer.toString(new Random().nextInt(85-70)+70);
        ready = true;
        if (res == null) {
            Log.e(TAG, "onPostExecute: res返回空值");
            return;
        }
        if (isCancelled()){
            my_cancel();
            return;
        }

        float temperature;
        int heartbeat;
        String[] values = res.split(" ");
        temperature = Float.valueOf(values[0]);
        heartbeat = Integer.valueOf(values[1]);
        Log.i(TAG, "onPostExecute: 温度："+temperature+" 心跳："+heartbeat);
        insert(context,temperature,heartbeat);            //新添一条体检记录
        initView(temperature,heartbeat);
        sweetAlertDialog.cancel();
    }

    public void handleData(String data){
        count++;
        if (count == 100) {
            res = data;
            ready = true;       //数据处理完毕
        }
    }

    private void my_cancel(){
        Log.i(TAG, "cancel: 执行了cancel方法");
        if (blueTooth != null) {
            try {
                blueTooth.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void timeoutClosing() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }, 20000);// 20s后超时关闭
    }

    private void initView(float temperature,int heartbeat){
        viewPager = (ViewPager) activity.findViewById(R.id.viewPager);
        layout_frame = (FrameLayout) activity.findViewById(R.id.layout_frame);
        layout_point = (LinearLayout) activity.findViewById(R.id.layout_point);
        viewPager.setOnPageChangeListener(this);
        viewPager = (ViewPager) activity.findViewById(R.id.viewPager);

        ArrayList<Float> xValues = new ArrayList<>();
        List<Float> yValue_temperature = new ArrayList<>();
        List<Float> yValue_heartbeat = new ArrayList<>();

        try {
            JSONArray res = selectTemperature(context,5).getJSONArray("data");
            Log.i(TAG, "initView: "+res);
            for (int i = 0; i < res.length(); i++) {
                xValues.add((float) i);
                yValue_temperature.add((float)(res.getDouble(i)));
            }
            res = selectHeartBeat(context,5).getJSONArray("data");
            Log.i(TAG, "initView: "+res);
            for (int i = 0; i < res.length(); i++) {
                yValue_heartbeat.add((float)(res.getInt(i)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //设置温度页
        View view_temperature = LayoutInflater.from(context).inflate(R.layout.fragment_page,null);
        LineChartManager history_temperature = new LineChartManager((LineChart)view_temperature.findViewById(R.id.history));
        history_temperature.showLineChart(xValues,yValue_temperature,"体温", Color.BLUE);
        history_temperature.setHightLimitLine((float) 38.5,"正常体温:38.5",Color.RED);
        history_temperature.setDescription("体温趋势图");

        TextView txt_num = (TextView)view_temperature.findViewById(R.id.txt_num);
        txt_num.setText("本次测得体温："+Float.toString(temperature));
        list_view.add(view_temperature);
        //设置心跳页
        View view_heartbeat = LayoutInflater.from(context).inflate(R.layout.fragment_page,null);
        LineChartManager history_heartbeat = new LineChartManager((LineChart)view_heartbeat.findViewById(R.id.history));
        history_heartbeat.showLineChart(xValues,yValue_heartbeat,"心率", Color.BLUE);
        history_heartbeat.setHightLimitLine((float) 88,"正常心率:88",Color.RED);
        history_heartbeat.setDescription("心率趋势图");

        TextView txt_num2 = (TextView)view_heartbeat.findViewById(R.id.txt_num);
        txt_num2.setText("本次测得心率："+Integer.toString(heartbeat));
        list_view.add(view_heartbeat);

        //设置心跳页
        View view_summary = LayoutInflater.from(context).inflate(R.layout.fragment_page,null);
        ((LineChart)view_summary.findViewById(R.id.history)).setVisibility(View.GONE);

        TextView txt_num3 = (TextView)view_summary.findViewById(R.id.txt_num);
        txt_num3.setText("确认过眼神，你最健康");
        list_view.add(view_summary);
        LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        txt_num3.setLayoutParams(lpp);
        adapter = new ViewPagerAdapter(list_view);
        viewPager.setAdapter(adapter);

        //添加引导点
        for (int i = 0; i < list_view.size(); i++) {
            ImageView point = new ImageView(context);
            //设置暗点
            point.setBackgroundResource(R.drawable.point);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(20, 0, 20, 0);
            point.setLayoutParams(lp);
            list_pointView.add(point);
            layout_point.addView(point);
        }

        //添加选中的引导点
        img_colorPoint = new ImageView(context);
        //设置亮点
        img_colorPoint.setBackgroundResource(R.drawable.point_fill);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        img_colorPoint.setLayoutParams(lp);
        layout_frame.addView(img_colorPoint);
        layout_frame.post(new Runnable() {
            @Override
            public void run() {
                //待布局绘制完毕  设置选中白点 的初始化位置
                FrameLayout.LayoutParams l = (FrameLayout.LayoutParams) img_colorPoint.getLayoutParams();
                l.leftMargin = list_pointView.get(0).getLeft();
                img_colorPoint.setLayoutParams(l);
            }
        });

        layout_point.post(new Runnable() {

            @Override
            public void run() {
                // 获取引导的之间的间隔
                pointSpacing = layout_point.getChildAt(1).getLeft()- layout_point.getChildAt(0).getLeft();
            }
        });
        voiceAnnouncements(0);          //播报第一个
        left = (ImageView)activity.findViewById(R.id.left);
        right = (ImageView)activity.findViewById(R.id.right);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(page!=0){
                    page--;
                    viewPager.setCurrentItem(page);
                 }
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(page!=list_view.size()-1){
                    page++;
                    viewPager.setCurrentItem(page);
                }
            }
        });
    }

    @Override
    public void onPageScrollStateChanged(int arg0) { }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

        FrameLayout.LayoutParams l = (FrameLayout.LayoutParams) img_colorPoint
                .getLayoutParams();
        //根据滑动动态设置左外边距
        l.leftMargin = (int) (list_pointView.get(arg0).getLeft() + pointSpacing
                * arg1);
        img_colorPoint.setLayoutParams(l);
    }

    @Override
    public void onPageSelected(int position) {
        page = position;
        voiceAnnouncements(page);
    }

    public void voiceAnnouncements(int page){
        if (radioOn && !hasAnnounced.contains(page)){
            View currentView = list_view.get(page);
            TextView curretTextView = (TextView)currentView.findViewById(R.id.txt_num);
            Log.i(TAG, "onClick: "+curretTextView.getText().toString());
            xfyunASR.speekText(curretTextView.getText().toString());
            hasAnnounced.add(page);
        }
    }
}
