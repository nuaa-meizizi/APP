package com.scy.health.util;

public class Measurement{
    /*
    根据指标返回string字符串，监测结果
    bp[0]:收缩压
    bp[1]:舒张压
     */
    public static String measureIndicator(float temperature, int heartbeat, int[] bp, String sex){
        String res = "";
        if (temperature > 37)
            res+="体温较正常人偏高\n";
        else if (temperature < 36)
            res+="体温较正常人偏低\n";
        if (heartbeat < 60)
            res+="心率较正常人偏高\n";
        else if (heartbeat > 100)
            res+="心率较正常人偏高\n";
        if (bp[0]>=140 || bp[1]>=90)
            res+="血压偏高\n";
        if (bp[0]<90 || bp[1]<60)
            res+="血压偏低\n";
        return res;
    }

    public static String driveMeasureIndicator(float temperature, int heartbeat, int[] bp, double[] eye, String sex){
        String res = "";
        if (temperature > 37)
            res+="体温较正常人偏高\n";
        else if (temperature < 36)
            res+="体温较正常人偏低\n";
        if (heartbeat < 60)
            res+="心率较正常人偏高\n";
        else if (heartbeat > 100)
            res+="心率较正常人偏高\n";
        if (bp[0]>=140 || bp[1]>=90)
            res+="血压偏高\n";
        if (bp[0]<90 || bp[1]<60)
            res+="血压偏低\n";
        return res;
    }
}
