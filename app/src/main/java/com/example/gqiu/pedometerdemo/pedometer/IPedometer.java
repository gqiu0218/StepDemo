package com.example.gqiu.pedometerdemo.pedometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorEventListener;

import java.util.TimeZone;

/**
 * 计步功能的基类，主要用于统一不同api版本的规范
 */
public abstract class IPedometer implements SensorEventListener {
    private static final int OFFSET = (1000 * 3600 * 24);
    protected SharedPreferences mSpUtil;
    private static final String EVERY_NOW_COUNTER_KEY = "NowCounterKey";
    public static final String EVERY_NATIVE_KEY = "NativeKey";
    public static final int MAX_STEP = 100000;          //每天最大值
    private long mLastTime;

    public IPedometer(Context context) {
        mSpUtil = context.getSharedPreferences("step", Context.MODE_PRIVATE);
    }


    /**
     * 注册感应器
     */
    public abstract void registerSensor();

    /**
     * 解除注册的感应器
     */
    public abstract void unregisterSensor();

    /**
     * 今天过完了，日期发生变化时触发。
     * <p>比如：由2016-11-04变成2016-11-05</p>
     */
    public abstract void onDateChange();


    //今天的步数(使用的步数)
    String getTodayNativeKey() {
        return EVERY_NATIVE_KEY + "_" + getTodayTime();
    }

    String getNowCounterKey() {
        return EVERY_NOW_COUNTER_KEY;
    }


    void save(int realStepNum) {
        mSpUtil.edit().putInt(getTodayNativeKey(), realStepNum).apply();
    }

    int getTodayStepNum() {
        return mSpUtil.getInt(getTodayNativeKey(), 0);
    }


    private long getTodayTime() {
        long current = System.currentTimeMillis();
        long result = current / OFFSET * OFFSET - TimeZone.getDefault().getRawOffset();

        if (result != mLastTime) {
            onDateChange();
        }

        mLastTime = result;
        return result;
    }
}