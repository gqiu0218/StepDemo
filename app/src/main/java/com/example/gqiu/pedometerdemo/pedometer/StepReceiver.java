package com.example.gqiu.pedometerdemo.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class StepReceiver extends BroadcastReceiver {
    public static final String STEP_KEY = "step";
    public static final String ACTION_UPDATE_STEP = "ACTION_UPDATE_STEP";
    public static final String ACTION_INIT_STEP = "ACTION_INIT_STEP";


    /**
     * 回调接口：步数发生变化时，调用些方法。
     */
    private StepListener mStepListener;

    public StepReceiver(StepListener stepListener) {
        this.mStepListener = stepListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 更新界面上的步数
        int num = intent.getIntExtra(STEP_KEY, 0);
        if (mStepListener != null) {
            mStepListener.onStepChange(num);
        }
    }

    /**
     * 默认注册步数更新广播，只接受更新广播,不能添加其他的action
     *
     * @param ctx 　上下文对象
     */
    public void register(Context ctx) {
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_STEP);
        ctx.registerReceiver(this, filter);
    }

    /**
     * 注册步数更新广播,可以为更新广播注册多个action
     *
     * @param ctx     上下文对象
     * @param actions 多个action
     */
    public void register(Context ctx, String... actions) {
        IntentFilter filter = new IntentFilter();
        for (int i = 0; i < actions.length; i++) {
            filter.addAction(actions[i]);
        }
        ctx.registerReceiver(this, filter);
    }

    /**
     * 解除绑定
     *
     * @param ctx 上下文对象
     */
    public void unregister(Context ctx) {
        ctx.unregisterReceiver(this);
    }

}
