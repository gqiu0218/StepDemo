package com.example.gqiu.pedometerdemo.pedometer;

/**
 * 计步器变化回调接口
 */
public interface IPedometerCallback {

    /**
     * 感应到步数发生变化时执行
     *
     * @param stepNum 步数(当天此时累计的步数)
     */
    void onSensorCounterChange(int stepNum);

    void onSensorDetectorChange(int addStepNum);

    void onDateChange();
}
