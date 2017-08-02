package com.inuker.recorder3;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by liwentian on 17/8/2.
 */

public class MyApplication extends Application {

    private static MyApplication INSTANCE;

    private static Handler sHandler;

    public static MyApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sHandler = new Handler(Looper.getMainLooper());
        INSTANCE = this;
    }

    public static void post(Runnable runnable) {
        sHandler.post(runnable);
    }
}
