package com.example.gqiu.pedometerdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.gqiu.pedometerdemo.pedometer.IPedometer;
import com.example.gqiu.pedometerdemo.pedometer.StepHelper;
import com.example.gqiu.pedometerdemo.pedometer.StepListener;
import com.example.gqiu.pedometerdemo.pedometer.StepReceiver;

import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements StepListener {
    private static final int OFFSET = (1000 * 3600 * 24);
    private TextView textView;
    private StepReceiver mReceiver;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv_step);
        StepHelper.startStep(this);

        mReceiver = new StepReceiver(this);
        mReceiver.register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("gqiu", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("gqiu", "onResume");
        SharedPreferences mSpUtil = getSharedPreferences("step", Context.MODE_PRIVATE);
        int stepNum = mSpUtil.getInt(IPedometer.EVERY_NATIVE_KEY + "_" + getTodayTime(), 0);
        textView.setText(String.valueOf(stepNum));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.unregister(this);
    }


    @Override
    public void onStepChange(final int stepNum) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                textView.setText(String.valueOf(stepNum));
            }
        });

    }


    long getTodayTime() {
        long current = System.currentTimeMillis();
        return current / OFFSET * OFFSET - TimeZone.getDefault().getRawOffset();
    }
}
