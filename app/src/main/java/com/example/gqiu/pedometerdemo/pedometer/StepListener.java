package com.example.gqiu.pedometerdemo.pedometer;

/**
 * 回调接口：步数发生变化时，调用些方法。
 * 使用说明：需要在Activty或者fragment中实现该接口，
 * 并注册StepReceiver后就会起作用
 */
public interface StepListener {

	/**
	 * 步数发生变化时执行
	 * @param stepNum 步数(当天此时累计的步数)
	 */
	void onStepChange(int stepNum);
}