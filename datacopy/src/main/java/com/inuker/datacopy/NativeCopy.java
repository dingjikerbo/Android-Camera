package com.inuker.datacopy;

import android.graphics.ImageFormat;

import com.inuker.library.BaseApplication;
import com.inuker.library.EventDispatcher;
import com.inuker.library.LogUtils;

/**
 * Created by liwentian on 17/8/24.
 */

public class NativeCopy {

    private static final int WRITE_DURATION = 1;
    private static final int READ_DURATION = 2;

    static {
        System.loadLibrary("copy");
    }

    public static void onCallback(int key, long time) {
//        switch (key) {
//            case WRITE_DURATION:
//                EventDispatcher.dispatch(Events.EVENTS_TIME_UPDATE, time);
//                break;
//            case READ_DURATION:
//                EventDispatcher.dispatch(Events.EVENTS_TIME_UPDATE, time);
//                break;
//        }
    }

    public static void init() {
        nativeInit(MyApplication.getScreenWidth() * MyApplication.getScreenHeight() * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8);
    }

    public static void write(byte[] bytes) {
        nativeWrite(bytes);
    }

    public static void read(byte[] bytes) {
        nativeRead(bytes);
    }

    private static native void nativeInit(int size);

    /**
     * Copy buffer from java to native
     */
    public static native void nativeWrite(byte[] bytes);

    /**
     * Copy buffer from native to java
     */
    public static native void nativeRead(byte[] bytes);
}
