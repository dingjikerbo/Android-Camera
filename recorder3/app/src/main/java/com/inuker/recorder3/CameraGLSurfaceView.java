package com.inuker.recorder3;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by liwentian on 17/7/28.
 */

public class CameraGLSurfaceView extends GLSurfaceView {

    public CameraGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setRenderer(new CameraSurfaceRender(this));
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
