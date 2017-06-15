package com.hobot.smarthome.testcamera;

import android.util.Log;

/**
 * Created by liwentian on 17/6/15.
 */

public class LogUtils {

    private static final String TAG = "bush";

    public static void v(String msg) {
        Log.v(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }
}
