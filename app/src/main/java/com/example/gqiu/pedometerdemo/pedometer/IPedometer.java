package com.example.gqiu.pedometerdemo.pedometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorEventListener;

import java.util.Calendar;

/**
 * 计步功能的基类，主要用于统一不同api版本的规范
 */
public abstract class IPedometer implements SensorEventListener {
    protected SharedPreferences mSpUtil;
    private static final String EVERY_NOW_COUNTER_KEY = "NowCounterKey";
    public static final String EVERY_NATIVE_KEY = "NativeKey";
    public static final int MAX_STEP = 100000;          //每天最大值
    private long mLastTime;
    private Calendar calendar;


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
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        long current = System.currentTimeMillis();
        calendar.setTimeInMillis(current);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long result = calendar.getTimeInMillis();
        if (result != mLastTime) {
            onDateChange();
        }

        mLastTime = result;
        return result;
    }
}