package com.example.gqiu.pedometerdemo.pedometer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 时间工具类
 * Created by gqiu on 2017/8/10.
 */

public class TimeUtils {

    public static long getTodayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

}
