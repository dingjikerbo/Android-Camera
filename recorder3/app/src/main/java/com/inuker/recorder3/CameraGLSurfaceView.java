package com.inuker.recorder3;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.inuker.recorder3.utils.CameraHelper;

import java.io.File;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by liwentian on 17/7/28.
 */

public class CameraGLSurfaceView extends GLSurfaceView {

    private CameraSurfaceRender mRender;

    public CameraGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        getHolder().setFormat(PixelFormat.RGBA_8888);

        mRender = new CameraSurfaceRender(this);
        setRenderer(mRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRender.onSurfaceDestroy();
            }
        });
    }

    public void startRecording() {
        File output = CameraHelper.getOutputVideoFile();
        mRender.startRecording(output);
    }

    public void stopRecording() {
        mRender.stopRecording();
    }
}
