package com.example.gqiu.pedometerdemo.pedometer;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

/**
 * 记步 工厂类
 */
 class PedometerFactory {

	 static IPedometer get(Context context, IPedometerCallback callback)  {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && isSupportCounterSensor(context)) {
			// ======使用计步感应器======
			// Android4.4 Kitkat 新增的STEP DETECTOR 以及 STEP COUNTER传感器。
			return new PedometerAPI19(context, callback);
		} else if (isSupportAcceleromoterSensor(context)) {
			// ======使用加速度感应器=======
			return new PedometerAPILt19(context, callback);
		} else {
			return null;
		}
	}

	/**
	 * 是否支持计步感应器
	 *
	 * @param context
	 *            上下文对象
	 * @return true为支持计数感应器,false
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private static boolean isSupportCounterSensor(Context context) {
		SensorManager mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor counterSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		Sensor detectorSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
		// 是否有计数感应器
		return counterSensor != null || detectorSensor != null;
	}

	/**
	 * 是否支持加速度感应器
	 *
	 * @param context
	 *            上下文对象
	 * @return true为支持加速度感应器,false
	 */
	private static boolean isSupportAcceleromoterSensor(Context context) {
		SensorManager mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// 是否有加速度感应器
		return sensor != null;
	}
}