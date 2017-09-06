package com.example.gqiu.pedometerdemo.pedometer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;


import com.example.gqiu.pedometerdemo.R;


/**
 * 记步服务
 */
public class StepService extends Service implements IPedometerCallback {
    /**
     * 计步实现对象
     */
    private IPedometer mPedometer;
    /**
     * 当天行走的步数
     */
    private int stepCounter = 0;
    /**
     * 通知栏管理类
     */
    private NotificationManager mNotificationMgr;

    private DateChangedReceiver mReceiver;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPedometer = PedometerFactory.get(getApplicationContext(), this);

        if (mPedometer == null) {
            Log.e("gqiu", "设备不支持记步功能");
        } else {
            stepCounter = mPedometer.getTodayStepNum();
            registerBroadcast();
            mPedometer.registerSensor();
            sendStep(stepCounter);
        }
        return START_STICKY;
    }

    /**
     * 更新UI
     *
     * @param stepNum 今天步数
     */
    private void sendStep(int stepNum) {
        updateNotification("今日步数：" + stepNum + " 步");// 更新通知栏通知
        StepHelper.sendStepReceiver(getApplicationContext(), stepNum);// 发送步数广播(更新主界面以及其他注册StepReceiver广播的界面的实时步数显示)
    }

    /**
     * 更新通知
     */
    private void updateNotification(String content) {
        if (mNotificationMgr == null) {
            mNotificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent intent = new Intent("cn.jpush.android.intent.NOTIFICATION_OPENED");
        // JPushReceiver 会检查 Bundle 对象，这里给它传一个空的 bundle，以防止 JPushReceiver 爆出空指针异常
        intent.putExtra(null, new Bundle());
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setTicker(getString(R.string.app_name));
        builder.setContentTitle(getString(R.string.app_name));
        //设置不可清除
        builder.setOngoing(true);
        builder.setContentText(content);
        Notification notification = builder.build();

        startForeground(0, notification);
        mNotificationMgr.notify(R.string.app_name, notification);
    }


    @Override
    public void onSensorCounterChange(int addStepNum) {
        Log.e("gqiu", "Counter step num:" + addStepNum);
        if (stepCounter == 0) {
            stepCounter = mPedometer.getTodayStepNum();
        }

        if ((stepCounter + addStepNum) < IPedometer.MAX_STEP) {
            stepCounter += addStepNum;
            //更新ui
            sendStep(stepCounter);
            mPedometer.save(stepCounter);
        }
    }

    @Override
    public void onSensorDetectorChange(int addStepNum) {
        if (stepCounter == 0) {
            stepCounter = mPedometer.getTodayStepNum();
        }

        if ((stepCounter + addStepNum) < IPedometer.MAX_STEP) {
            stepCounter += addStepNum;
            sendStep(stepCounter);
            Log.e("gqiu", "Detector step num:" + stepCounter);
            mPedometer.save(stepCounter);
        }
    }


    @Override
    public void onDestroy() {
        //取消前台进程
        stopForeground(true);
        //取消感应器
        if (mPedometer != null) {
            mPedometer.unregisterSensor();
        }

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(StepReceiver.ACTION_INIT_STEP);
        mReceiver = new DateChangedReceiver();
        registerReceiver(mReceiver, filter);
    }


    private class DateChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            switch (action) {
                case Intent.ACTION_DATE_CHANGED:
                    if (mPedometer != null) {
                        mPedometer.onDateChange();
                        stepCounter = 0;
                    }
                    break;
                case StepReceiver.ACTION_INIT_STEP:
                    stepCounter = 0;
                    break;
            }

        }
    }
}