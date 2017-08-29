package com.inuker.recorder1;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.inuker.library.BaseActivity;
import com.inuker.library.BaseApplication;
import com.inuker.library.CameraHelper;
import com.inuker.library.MediaEncoderCore;
import com.inuker.library.MovieEncoder;

import java.io.File;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private FrameLayout mSurfaceContainer;
    private GLSurfaceView mGLSurfaceView;
    private Button mBtn;

    private CameraSurfaceRender mRender;

    private boolean mRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceContainer = (FrameLayout) findViewById(R.id.surface);
        mBtn = (Button) findViewById(R.id.btn);

        mBtn.setOnClickListener(this);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mRender = new CameraSurfaceRender(mGLSurfaceView);
        mGLSurfaceView.setRenderer(mRender);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onPause() {
        mSurfaceContainer.removeAllViews();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceContainer.addView(mGLSurfaceView);
    }

    @Override
    public void onClick(View v) {
        if (mRecording) {
            mRecording = false;
            mBtn.setText("start");
            mRender.stopRecording();
        } else {
            mRecording = true;
            mRender.startRecording();
            mBtn.setText("stop");
        }
    }
}
