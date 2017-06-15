package com.hobot.smarthome.testcamera;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        GLSurfaceView surfaceView = new GLSurfaceView(this);
        setContentView(surfaceView);

        surfaceView.setEGLContextClientVersion(2);
        surfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        MyRender render = new MyRender(surfaceView);
        surfaceView.setRenderer(render);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
}
