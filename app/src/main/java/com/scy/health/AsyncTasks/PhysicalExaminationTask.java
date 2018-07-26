package com.scy.health.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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
import com.scy.health.Interface.DataBroadcastInterface;
import com.scy.health.Interface.MeasurementInterface;
import com.scy.health.R;
import com.scy.health.ViewPagerAdapter;
import com.scy.health.util.DataBroadcast;
import com.scy.health.util.LineChartManager;
import com.scy.health.util.Measurement;
import com.scy.health.util.XfyunASR;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.scy.health.util.SharedPreferencesDataBase.insert;
import static com.scy.health.util.SharedPreferencesDataBase.selectBp;
import static com.scy.health.util.SharedPreferencesDataBase.selectHeartBeat;
import static com.scy.health.util.SharedPreferencesDataBase.selectTemperature;

public class PhysicalExaminationTask extends AsyncTask<String, Void, String>  implements ViewPager.OnPageChangeListener,DataBroadcastInterface {
    private static final String TAG = "PhysicalExaminationTask";
    private Context context;
    private Activity activity;
    private int count;          //接受数据次数
    private SweetAlertDialog sweetAlertDialog;
    private ViewPager viewPager;
    private FrameLayout layout_frame;
    private LinearLayout layout_point;
    private ViewPagerAdapter adapter;
    private List<View> list_view = new ArrayList<View>();
    private List<ImageView> list_pointView = new ArrayList<ImageView>();
    private ImageView img_colorPoint,left,right;
    // 两点之间间距
    private int pointSpacing;
    private DataBroadcast dataBroadcast;
    private int page = 0;
    private XfyunASR xfyunASR;
    private boolean radioOn;        //是否开启语音播报
    private Set<Integer> hasAnnounced = new HashSet<Integer>();
    private String sex;
    private float temperature;
    private int heartbeat;
    private int[] bp;
    private  TextView txt_num4;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initView(temperature,heartbeat,bp,sex);
                    sweetAlertDialog.cancel();
                    break;
                case 2:
                    txt_num4.setText((String )msg.obj);
                    break;
                case 3:
                    Toast.makeText(context,"蓝牙连接成功",Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(context,"蓝牙连接错误："+(String) msg.obj,Toast.LENGTH_SHORT).show();
                    sweetAlertDialog.cancel();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public PhysicalExaminationTask(Context context, SweetAlertDialog sweetAlertDialog, XfyunASR xfyunASR)
    {
        this.context = context;
        this.activity = (Activity)context;
        this.sweetAlertDialog = sweetAlertDialog;
        this.xfyunASR = xfyunASR;
        SharedPreferences sp = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        this.radioOn = sp.getBoolean("radio",false);
        this.sex = sp.getString("sex","男");
        Log.i(TAG, "PhysicalExaminationTask: "+radioOn);
    }

    @Override
    protected String doInBackground(String... strings) {
        if (isCancelled()){
            my_cancel();
            return null;
        }
        Looper.prepare();
        dataBroadcast = new DataBroadcast(context,this);
        Looper.loop();
        return null;
    }

    private void my_cancel(){
        if (dataBroadcast != null) {
            Log.i(TAG, "cancel: 执行了cancel方法");
            dataBroadcast.destroy();
            dataBroadcast = null;
        }
    }

    private void initView(float temperature,int heartbeat,int[] bp,String sex){
        viewPager = (ViewPager) activity.findViewById(R.id.viewPager);
        layout_frame = (FrameLayout) activity.findViewById(R.id.layout_frame);
        layout_point = (LinearLayout) activity.findViewById(R.id.layout_point);
        viewPager.setOnPageChangeListener(this);
        viewPager = (ViewPager) activity.findViewById(R.id.viewPager);

        ArrayList<Float> xValues = new ArrayList<>();
        List<Float> yValue_temperature = new ArrayList<>();
        List<Float> yValue_heartbeat = new ArrayList<>();
        List<List<Float>> yValue_bp = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        names.add("收缩压");
        names.add("舒张压");
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        try {
            JSONArray res = selectTemperature(context,5).getJSONArray("data");
            Log.i(TAG, "initView Temperature:"+res);
            for (int i = 0; i < res.length(); i++) {
                xValues.add((float) i);
                yValue_temperature.add((float)(res.getDouble(i)));
            }
            res = selectHeartBeat(context,5).getJSONArray("data");
            Log.i(TAG, "initView HeartBeat: "+res);
            for (int i = 0; i < res.length(); i++) {
                yValue_heartbeat.add((float)(res.getInt(i)));
            }

            res = selectBp(context,5).getJSONArray("data");
            List<Float> bp0list = new ArrayList<>();
            List<Float> bp1list = new ArrayList<>();

            Log.i(TAG, "initView bp:"+res);
            for (int i = 0; i < res.length(); i++) {
                JSONObject jsonObject = (res.getJSONObject(i));
                bp0list.add((float) jsonObject.getInt("0"));
                bp1list.add((float) jsonObject.getInt("1"));
            }
            yValue_bp.add(bp0list);
            yValue_bp.add(bp1list);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //设置温度页
        View view_temperature = LayoutInflater.from(context).inflate(R.layout.fragment_page,null);
        LineChartManager history_temperature = new LineChartManager((LineChart)view_temperature.findViewById(R.id.history));
        history_temperature.showLineChart(xValues,yValue_temperature,"体温", Color.BLUE);
//        history_temperature.setHightLimitLine((float) 38.5,"正常体温:38.5",Color.RED);
        history_temperature.setDescription("体温趋势图");

        TextView txt_num = (TextView)view_temperature.findViewById(R.id.txt_num);
        txt_num.setText("本次测得体温："+Float.toString(temperature)+"℃");
        list_view.add(view_temperature);

        //设置心跳页
        View view_heartbeat = LayoutInflater.from(context).inflate(R.layout.fragment_page,null);
        LineChartManager history_heartbeat = new LineChartManager((LineChart)view_heartbeat.findViewById(R.id.history));
        history_heartbeat.showLineChart(xValues,yValue_heartbeat,"心率", Color.BLUE);
//        history_heartbeat.setHightLimitLine((float) 88,"正常心率:88",Color.RED);
        history_heartbeat.setDescription("心率趋势图");

        TextView txt_num2 = (TextView)view_heartbeat.findViewById(R.id.txt_num);
        txt_num2.setText("本次测得心率："+Integer.toString(heartbeat)+"次/分钟");
        list_view.add(view_heartbeat);


        //设置血压页
        View view_bp = LayoutInflater.from(context).inflate(R.layout.fragment_page,null);
        LineChartManager history_bp = new LineChartManager((LineChart)view_bp.findViewById(R.id.history));
        history_bp.showLineChart(xValues,yValue_bp,names, colors);
        history_bp.setDescription("血压趋势图");

        TextView txt_num3 = (TextView)view_bp.findViewById(R.id.txt_num);
        txt_num3.setText("本次测得血压\n收缩压："+Integer.toString(bp[0])+"mmHg\n舒张压："+Integer.toString(bp[1])+"mmHg");
        list_view.add(view_bp);

        //设置报告页
        View view_summary = LayoutInflater.from(context).inflate(R.layout.fragment_page,null);
        ((LineChart)view_summary.findViewById(R.id.history)).setVisibility(View.GONE);

        txt_num4 = (TextView)view_summary.findViewById(R.id.txt_num);

        new Measurement().measureIndicator(new MeasurementInterface() {
            @Override
            public void onSuccess(String res) {
                Message message = new Message();
                message.what = 2;
                message.obj = res;
                handler.sendMessage(message);
            }

            @Override
            public void onError(String errorData) {

            }
        },temperature, heartbeat, bp, sex);
        LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        txt_num4.setLayoutParams(lpp);
        list_view.add(view_summary);

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

        FrameLayout.LayoutParams l = (FrameLayout.LayoutParams) img_colorPoint.getLayoutParams();
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

    @Override
    public void onaTemperatureChanged(float temperature) {

    }

    @Override
    public void onHeartbeatChanged(int heartbeat) {

    }

    @Override
    public void onBpChanged(int[] bp) {

    }

    @Override
    public void onChanged(float temperature, int heartbeat, int[] bp, double[] eye) {
        count++;
        if (isCancelled()){
            my_cancel();
            return;
        }
        if (count == 10) {
            this.temperature = temperature;
            this.heartbeat = heartbeat;
            this.bp = bp;
            insert(context,temperature,heartbeat,45,bp,0);
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
            if (dataBroadcast!=null)
                dataBroadcast.destroy();
        }
    }

    @Override
    public void onEyeChanged(double[] eye) {

    }

    @Override
    public void onSuccess() {
        Message message = new Message();
        message.what = 3;
        handler.sendMessage(message);
    }

    @Override
    public void onOverTime(String error) {
        Message message = new Message();
        message.what = 4;
        message.obj = error;
        handler.sendMessage(message);
    }
}
