package com.example.gqiu.pedometerdemo.pedometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.CountDownTimer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 依赖 加速度传感器 来记录步数信息
 */
class PedometerAPILt19 extends IPedometer {

    private Timer timer;
    private TimeCount time;

    // 存放三轴数据
    float[] oriValues = new float[3];
    final int valueNum = 4;
    // 用于存放计算阈值的波峰波谷差值
    float[] tempValue = new float[valueNum];
    int tempCount = 0;
    // 是否上升的标志位
    boolean isDirectionUp = false;
    // 持续上升次数
    int continueUpCount = 0;
    // 上一点的持续上升的次数，为了记录波峰的上升次数
    int continueUpFormerCount = 0;

    // 上一点的状态，上升还是下降
    boolean lastStatus = false;
    // 波峰值
    float peakOfWave = 0;
    // 波谷值
    float valleyOfWave = 0;
    // 此次波峰的时间
    long timeOfThisPeak = 0;
    // 上次波峰的时间
    long timeOfLastPeak = 0;
    // 当前的时间
    long timeOfNow = 0;
    // 当前传感器的值
    float gravityNew = 0;
    // 上次传感器的值
    float gravityOld = 0;
    // 动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    final float initialValue = (float) 1.7;
    // 初始阈值
    float ThreadValue = (float) 2.0;

    /**
     * 0-准备计时   1-计时中   2-正常计步中
     */
    private int countTimeState = 0;
    private int currentStep = 0;// 当前步数
    private int tempStep = 0;
    private int lastStep = -1;
    // 加速计的三个维度数值
    public static float[] gravity = new float[3];
    public static float[] linear_acceleration = new float[3];
    // 用三个维度算出的平均值
    public static float average = 0;
    // 倒计时3秒，3秒内不会显示计步，用于屏蔽细微波动
    private long duration = 2500;

    // private UserInfo userInfo;// 用户信息
    static int oldEnergy;// 上一次记录能量
    boolean hasSendToService;// 是否要进行上传步数操作的变量 false为上传
    public static int ENERGY_SETP = 0;// 当前能量
    public static int TASK_SETP = 0;// 当前任务步数
    public static int ALARM_STEP = 0;// 记时步数，5分钟存一次数据库

    // 感应器Manager
    private SensorManager mSensorMgr;
    // 计步数感应器
    private Sensor mSensorAccelerometer;
    private IPedometerCallback mCallback;

    PedometerAPILt19(Context context, IPedometerCallback callback) {
        super(context);
        init(context);
        this.mCallback = callback;
    }

    /**
     * 初始化操作
     *
     * @param context 上下文对象
     */
    private void init(Context context) {
        // 初始化感应器
        mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                calcStep(event);
            }
        }
    }

    private synchronized void calcStep(SensorEvent event) {
        average = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        detecNewStep(average);
    }

    private void preStep() {
        if (countTimeState == 0) {
            // 开启计时器
            time = new TimeCount(duration, 700);
            time.start();
            countTimeState = 1;


        } else if (countTimeState == 1) {
            tempStep++;
        } else if (countTimeState == 2) {
            currentStep++;
            if (mCallback != null) {
                // 回调处理
                mCallback.onSensorDetectorChange(1);
            }
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 如果计时器正常结束，则开始计步
            time.cancel();
            currentStep += tempStep;
            lastStep = -1;
//            LogUtils.i("+++++++++++++计时正常结束");

            timer = new Timer(true);
            TimerTask task = new TimerTask() {
                public void run() {
                    if (lastStep == currentStep) {
                        timer.cancel();
                        countTimeState = 0;
                        lastStep = -1;
                        tempStep = 0;
//                        LogUtils.i("++++++++++++++停止计步：" + CURRENT_SETP);
                    } else {
                        lastStep = currentStep;
//                        LogUtils.e("WALKUP---4=" + CURRENT_SETP);
                    }
                }
            };
            timer.schedule(task, 0, 2000);
            countTimeState = 2;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (lastStep == tempStep) {
//                LogUtils.i("+++++++++++++++onTick 计时停止");
                time.cancel();
                countTimeState = 0;
                lastStep = -1;
                tempStep = 0;
            } else {
                lastStep = tempStep;
            }
        }

    }

    /**
     * <p>检测步子，并开始计步 </p>
     * <p>1.传入sersor中的数据 </p>
     * <p>2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步</p>
     * <p>3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中</p>
     */
    public void detecNewStep(float avg) {
        //是否波峰
        boolean peak = isPeak(avg, gravityOld);
        if (peak) {
            timeOfLastPeak = timeOfThisPeak;
            timeOfNow = System.currentTimeMillis();
            //距离上次波峰时间过去了多久
            long dtLastPeak = timeOfNow - timeOfLastPeak;
            //波峰与波谷的差值
            float dtValue = peakOfWave - valleyOfWave;
            if (dtLastPeak >= 200 && dtLastPeak <= 1000 && (dtValue >= ThreadValue)) {
                timeOfThisPeak = timeOfNow;
                // 更新界面的处理，不涉及到算法
                preStep();
            }
            if (dtLastPeak >= 200 && (dtValue >= initialValue)) {
                timeOfThisPeak = timeOfNow;
                ThreadValue = getThreshold(dtValue);
            }
        }
        gravityOld = avg;
    }

    /**
     * <p>检测波峰 以下四个条件判断为波峰： </p>
     * <p>1.目前点为下降的趋势：isDirectionUp为false</p>
     * <p>2.之前的点为上升的趋势：lastStatus为true </p>
     * <p>3.到波峰为止，持续上升大于等于2次 </p>
     * <p>4.波峰值大于1.2g,小于2g </p>
     * <p>记录波谷值</p>
     * <p>1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值 </p>
     * <p>2.所以要记录每次的波谷值，为了和下次的波峰做对比</p>
     */
    public boolean isPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            isDirectionUp = false;
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
        }

        if (!isDirectionUp && lastStatus && (continueUpFormerCount >= 2 && (oldValue >= 11.76 && oldValue < 19.6))) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }

    /**
     * <p> 阈值的计算 </p>
     * <p> 1.通过波峰波谷的差值计算阈值 </p>
     * <p> 2.记录4个值，存入tempValue[]数组中</p>
     * <p> 3.在将数组传入函数averageValue中计算阈值</p>
     */
    public float getThreshold(float value) {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;
    }

    /**
     * <p> 梯度化阈值 </p>
     * <p> 1.计算数组的均值 </p>
     * <p> 2.通过均值将阈值梯度化在一个范围里</p>
     */
    public float averageValue(float value[], int n) {
        float avg = 0;
        for (int i = 0; i < n; i++) {
            avg += value[i];
        }
        avg = avg / valueNum;
        if (avg >= 8)
            avg = 4.3f;
        else if (avg >= 7 && avg < 8)
            avg = 3.3f;
        else if (avg >= 4 && avg < 7)
            avg = 2.3f;
        else if (avg >= 3 && avg < 4)
            avg = 2.0f;
        else
            avg = 1.3f;
        return avg;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void registerSensor() {
        // 优先使用counter感应器
        if (mSensorAccelerometer != null) {
            mSensorMgr.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }


    @Override
    public void unregisterSensor() {
        mSensorMgr.unregisterListener(this);
    }


    @Override
    public void onDateChange() {
        //今天过完了，日期发生变化，清零
        currentStep = 0;
        mInitTodayStepNum = 0;
        resetTodayTime();
    }

}
