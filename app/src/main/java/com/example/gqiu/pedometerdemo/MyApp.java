package com.example.gqiu.pedometerdemo;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.stetho.Stetho;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        Timber.plant(new Timber.DebugTree());
        Timber.plant(new CrashReportingTree(this));
    }


    private static class CrashReportingTree extends Timber.Tree {
        private Context context;
        private SimpleDateFormat mFormat;

        public CrashReportingTree(Context context) {
            this.context = context;
            mFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA);
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority != Log.ERROR) {
                return;
            }

            Date nowDate = new Date();
            String dateFormat = "[" + mFormat.format(nowDate) + "] ";
            message = dateFormat + message;
            Log.e("gqiu", message);

            File storeFileDir = context.getExternalFilesDir(null);
            if (storeFileDir == null) {
                storeFileDir = context.getCacheDir();
            }
            String storeFilePath = storeFileDir.getAbsolutePath();
            if (TextUtils.isEmpty(storeFilePath)) {
                return;
            }
            File file = new File(storeFilePath + File.separator + "log.txt");
            FileWriter writer;
            BufferedWriter bufferedWriter = null;
            try {
                writer = new FileWriter(file, true);
                bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.write(message + "\n");
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
