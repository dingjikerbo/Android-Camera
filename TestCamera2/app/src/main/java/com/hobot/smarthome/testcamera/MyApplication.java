package com.hobot.smarthome.testcamera;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by liwentian on 17/6/23.
 */

public class MyApplication extends Application {

    private static int mScreenWidth, mScreenHeight;

    @Override
    public void onCreate() {
        super.onCreate();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        mScreenWidth = Math.max(width, height);
        mScreenHeight = Math.min(width, height);

        Log.v("bush", String.format("w = %d, h = %d", mScreenWidth, mScreenHeight));
    }

    public static int getScreenWidth() {
        return mScreenWidth;
    }

    public static int getScreenHeight() {
        return mScreenHeight;
    }
}
