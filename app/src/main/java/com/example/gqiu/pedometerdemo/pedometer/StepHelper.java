package com.example.gqiu.pedometerdemo.pedometer;

import android.content.Context;
import android.content.Intent;


/**
 * 计步工具类
 */
public class StepHelper {

    /**
     * 开启计步功能，注意最好在app的第一个activity中调用
     *
     * @param context 上下文对象
     */
    public static void startStep(Context context) {
        Intent intent = new Intent(context, StepService.class);
        context.startService(intent);
    }

    /**
     * 关闭计步功能
     *
     * @param context 上下文对象
     */
    public static void stopStep(Context context) {
        context.stopService(new Intent(context, StepService.class));
    }

    /**
     * 发送更新步数变化的广播,通知更新ui
     *
     * @param ctx     上下文对象
     * @param stepNum 当天的步数
     */
    public static void sendStepReceiver(Context ctx, int stepNum) {
        Intent intent = new Intent(StepReceiver.ACTION_UPDATE_STEP);
        intent.putExtra(StepReceiver.STEP_KEY, stepNum);
        ctx.sendBroadcast(intent);
    }
}
