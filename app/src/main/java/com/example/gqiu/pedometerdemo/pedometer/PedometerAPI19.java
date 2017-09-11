package com.example.gqiu.pedometerdemo.pedometer;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import timber.log.Timber;


/**
 * 针对android4.4 版本进行的计步器功能
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
class PedometerAPI19 extends IPedometer {


    /**
     * 传感器批处理时间，最长 10 秒从传感器返回一次数据
     */
    private static final int BATCH_DELAY_TIME = 10 * 1000 * 1000;
    /**
     * 感应器Manager
     */
    private SensorManager mSensorMgr;
    /**
     * 计步数感应器
     */
    private Sensor mSensorStepCounter;
    /**
     * 探测器感应器
     */
    private Sensor mSensorStepDetector;

    private int mCounter;
    private int mDetector;
    private int mLastAdd;        //上一次增加的值
    private boolean sensorReset; //传感器是否重置

    /**
     * 是否为第一次获取到步数，
     * 因为这个感应器的计步是从开机计起来的，
     * 所以第一个步数＝第2次获取的步数-第1次获取的步数
     */
    private IPedometerCallback mCallback;

    /**
     * 带回调处理功能的构造方法
     *
     * @param context  上下文对象
     * @param callback 回调处理
     */
    PedometerAPI19(Context context, IPedometerCallback callback) {
        super(context);
        this.mCallback = callback;
        init(context);
    }

    /**
     * 获取记步传感器
     *
     * @param context 上下文对象
     */
    private void init(Context context) {
        mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorStepDetector = mSensorMgr.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mSensorStepCounter = mSensorMgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        boolean result = mSpUtil.contains(getTodayNativeKey());
        if (result) {
            mLastAdd = mSpUtil.getInt(getNowCounterKey(), 0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 获取类型
        int type = event.sensor.getType();

        if (Sensor.TYPE_STEP_DETECTOR == type) {
            Log.e("gqiu", "mDetector=" + mDetector);
            // 步行探测器
            if (event.values[0] == 1.0f) {
                mDetector++;
                if (mCallback != null) {
                    mCallback.onSensorDetectorChange(1);
                }
            }
        } else if (Sensor.TYPE_STEP_COUNTER == type) {
            // 获取步数
            int temp = Math.round(event.values[0]);
            if (temp == 0 && sensorReset) {
                //当重新初始值为0时认为是重新计算了
                sensorReset = false;
                mLastAdd = 0;
                mCounter = -1;
            }

            //去重复
            if (mCounter != temp) {
                mCounter = temp;
                if (!hasTodayNum()) {
                    /*是否为第一次获取到步数，
                     * 因为这个感应器的计步是从开机计起来的，
					 * 所以第一个步数＝第2次获取的步数-第1次获取的步数
					 */
                    Timber.e("=============================初始化新的一天===================================");
                    Timber.e("counter=" + mCounter);
                    mCallback.onDateChange();
                    mSpUtil.edit().putInt(getTodayNativeKey(), 0).apply();
                } else if (mCallback != null) {
                    // 回调处理
                    Timber.e("counter=" + mCounter + ",last=" + mLastAdd);
                    int addStepNum = mCounter - mLastAdd;  //减去上次add值
                    mLastAdd = mCounter;
                    if (addStepNum > 0)
                        mCallback.onSensorCounterChange(addStepNum);
                }
                mLastAdd = mCounter;
                mSpUtil.edit().putInt(getNowCounterKey(), mCounter).apply();
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //每当传感器精确度发生变化的时候，清空记录今日步数的map,因为当精度发生变化的时候，可能导致步数减少
        Timber.e("\nThe step sensor has change accuracy!");
        sensorReset = true;
    }

    @Override
    public void registerSensor() {
        //优先使用counter感应器
        if (mSensorStepCounter != null) {
            mSensorMgr.registerListener(this, mSensorStepCounter, SensorManager.SENSOR_DELAY_FASTEST, BATCH_DELAY_TIME);
        } else {
            mSensorMgr.registerListener(this, mSensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST, BATCH_DELAY_TIME);
        }
    }

    @Override
    public void unregisterSensor() {
        mSensorMgr.unregisterListener(this);
    }

    @Override
    public void onDateChange() {
        Timber.e("onDateChange:new day coming！");
        mCallback.onDateChange();
        //今天过完了，日期发生变化，清零
//        mCounter = 0;
//        mDetector = 0;
//
//
//        Calendar calendar = Calendar.getInstance();
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//        int second = calendar.get(Calendar.SECOND);
//        int mills = calendar.get(Calendar.MILLISECOND);
//
//        if (hour == 0 && minute == 0 && second == 0 && mills == 0) {
//            mLastAdd = 0;
//            mSpUtil.edit().putInt(getNowCounterKey(), 0).apply();
//        }
    }


    private boolean hasTodayNum() {
        return mSpUtil.contains(getTodayNativeKey());
    }
}
