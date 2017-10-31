package com.inuker.recorder2;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity implements View.OnClickListener {

    private CameraSurfaceView mSurfaceView;

    private FrameLayout mSurfaceContainer;
    private Button mBtn;

    private boolean mRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceContainer = (FrameLayout) findViewById(R.id.surface);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);

        mSurfaceView = new CameraSurfaceView(this);
    }

    @Override
    protected void onPause() {
        mSurfaceContainer.removeAllViews();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceContainer.addView(mSurfaceView);
    }

    @Override
    public void onClick(View v) {
        if (mRecording) {
            mRecording = false;
            mSurfaceView.stopRecording();
            mBtn.setText("start");
        } else {
            mRecording = true;
            mSurfaceView.startRecording();
            mBtn.setText("stop");
        }
    }
}
