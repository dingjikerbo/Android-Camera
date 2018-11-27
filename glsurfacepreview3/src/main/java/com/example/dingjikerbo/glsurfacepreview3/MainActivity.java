package com.example.dingjikerbo.glsurfacepreview3;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);

        Log.v("bush", "hello world");

        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(new CameraSurfaceRender(mGLSurfaceView));
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }
}
