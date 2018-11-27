package com.inuker.library.utils;

/**
 * Created by liwentian on 17/8/22.
 */

public class NativeUtils {

    static {
        System.loadLibrary("camera");
    }

    public static native void glReadPixels(int x, int y, int width, int height, int format, int type);
}
