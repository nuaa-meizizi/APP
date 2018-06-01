package com.scy.health.activities;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scy.health.R;
import com.scy.health.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class physicalExamination extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private FrameLayout layout_frame;
    LinearLayout layout_point;
    private List<View> list_view = new ArrayList<View>();
    List<ImageView> list_pointView = new ArrayList<ImageView>();
    ViewPagerAdapter adapter;

    ImageView img_colorPoint,left,right;
    // 两点之间间距
    int pointSpacing;

    int page = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_examination);

        new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setContentText("体检中")
                .show();
        initView();
    }

    public void initView(){
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        layout_frame = (FrameLayout) findViewById(R.id.layout_frame);
        layout_point = (LinearLayout) findViewById(R.id.layout_point);
        for (int i = 0; i < 4; i++){
            View view = LayoutInflater.from(this).inflate(R.layout.fragment_page,null);
            TextView txt_num = (TextView)view.findViewById(R.id.txt_num);
            txt_num.setText(i + "");
            list_view.add(view);
        }
        adapter = new ViewPagerAdapter(list_view);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(this);

        //添加引导点
        for (int i = 0; i < list_view.size(); i++) {
            ImageView point = new ImageView(this);
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
        img_colorPoint = new ImageView(physicalExamination.this);
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

        left = (ImageView)findViewById(R.id.left);
        right = (ImageView)findViewById(R.id.right);
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
    public void onPageScrollStateChanged(int arg0) {
    }

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
    }

}
