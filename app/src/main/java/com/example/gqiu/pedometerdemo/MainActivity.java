package com.example.gqiu.pedometerdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.gqiu.pedometerdemo.pedometer.StepHelper;
import com.example.gqiu.pedometerdemo.pedometer.StepListener;
import com.example.gqiu.pedometerdemo.pedometer.StepReceiver;

public class MainActivity extends AppCompatActivity implements StepListener {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("gqiu", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("gqiu", "onDestroy");
        StepHelper.stopStep(this);
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
}
