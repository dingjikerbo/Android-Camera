package com.inuker.library;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.view.WindowManager;

/**
 * Created by liwentian on 17/8/16.
 */

public class BaseApplication extends Application {

    private static BaseApplication sInstance;

    private static int mScreenWidth, mScreenHeight;

    private static float mDensity;

    private static Handler mHandler;

    public static BaseApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        mScreenWidth = Math.max(width, height);
        mScreenHeight = Math.min(width, height);

        mDensity = getResources().getDisplayMetrics().density;

        mHandler = new Handler();
    }

    public static int getScreenWidth() {
        return mScreenWidth;
    }

    public static int getScreenHeight() {
        return mScreenHeight;
    }

    public static int dp2px(int dp) {
        return (int) (dp *  mDensity + 0.5f);
    }

    public static void post(Runnable runnable) {
        mHandler.post(runnable);
    }
}
