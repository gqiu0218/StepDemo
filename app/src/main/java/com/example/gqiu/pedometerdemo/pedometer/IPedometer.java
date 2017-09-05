package com.example.gqiu.pedometerdemo.pedometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorEventListener;

/**
 * 计步功能的基类，主要用于统一不同api版本的规范
 */
abstract class IPedometer implements SensorEventListener {
    protected SharedPreferences mSpUtil;
    private static final String EVERY_COUNTER_KEY = "CounterKey";
    private static final String EVERY_NOW_COUNTER_KEY = "NowCounterKey";
    private static final String EVERY_NATIVE_KEY = "NativeKey";
    public static final int MAX_STEP = 100000;          //每天最大值
    private long mTodayKey;
    int mInitTodayStepNum;


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


    String getTodayCounterKey() {
        return EVERY_COUNTER_KEY + "_" + getTodayTime();
    }

    String getTodayNativeKey() {
        return EVERY_NATIVE_KEY + "_" + getTodayTime();
    }

    String getNowCounterKey() {return EVERY_NOW_COUNTER_KEY;}

    /**
     * 查看是否有今天的数据，如果有下次计步，否则将些次步数放入map
     * 如果map中虽然有记录，但是没有今天的第一步，则清除。然后记录今天的第一步
     *
     * @return true表示没有今天第一步的值，需要设置
     */


    void save(int realStepNum) {
        mSpUtil.edit().putInt(getTodayNativeKey(), realStepNum).apply();
    }


    long getTodayTime() {
        if (mTodayKey == 0) {
            mTodayKey = TimeUtils.getTodayTime();
        }
        return mTodayKey;
    }


    int initTodayStepNum() {
        if (mInitTodayStepNum == 0) {
            mInitTodayStepNum = mSpUtil.getInt(getTodayNativeKey(), 0);
        }
        return mInitTodayStepNum;
    }

    void resetTodayTime() {
        mTodayKey = 0;
        mInitTodayStepNum = 0;
    }

}