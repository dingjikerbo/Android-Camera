package com.hobot.smarthome.testcamera;

/**
 * Created by liwentian on 17/6/15.
 */

public class GPUImageNativeLibrary {

    static {
        System.loadLibrary("gpuimage-library");
    }

    public static native void YUVtoRBGA(byte[] yuv, int width, int height, int[] out);

    public static native void YUVtoARBG(byte[] yuv, int width, int height, int[] out);
}
