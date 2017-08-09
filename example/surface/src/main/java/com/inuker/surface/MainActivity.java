package com.inuker.surface;

import android.app.Activity;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.inuker.library.EglCore;
import com.inuker.library.EglSurfaceBase;
import com.inuker.library.WindowSurface;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

    private EglCore mEglCore;

    private WindowSurface mSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SurfaceView surface = new SurfaceView(this);
        setContentView(surface);

        surface.getHolder().addCallback(this);

        mEglCore = new EglCore();
        mSurface = new WindowSurface(mEglCore, surface.getHolder().getSurface(), false);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurface.makeCurrent();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 1f, 1f, 1f);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
