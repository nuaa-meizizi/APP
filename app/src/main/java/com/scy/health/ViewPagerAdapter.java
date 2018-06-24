package com.scy.health;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private List<View> views;
    private View mCurrentView;

    public ViewPagerAdapter(List<View> list) {
        super();
        this.views = list;
    }

    // 从当前container中删除指定位置（position）的View
    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView(views.get(position));
    }

    // 第一：将当前视图添加到container中，第二：返回当前View
    @Override
    public Object instantiateItem(View container, int position) {
        // 将当前视图添加到container中
        ((ViewPager) container).addView(views.get(position));
        // 设置当前视图的唯一标示Key
        return views.get(position);
    }

    // getCount():返回要滑动的View的个数
    @Override
    public int getCount() {
        return views.size();
    }

    // 通过标识arg1找到view
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

}
