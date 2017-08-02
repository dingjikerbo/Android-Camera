package com.inuker.recorder3;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button mBtnRecord;

    private CameraGLSurfaceView mSurface;

    private boolean mRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurface = (CameraGLSurfaceView) findViewById(R.id.surface);
        mBtnRecord = (Button) findViewById(R.id.btn);

        mBtnRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBtnClick();
            }
        });
    }

    private void onBtnClick() {
        if (mRecording) {
            mSurface.stopRecording();
            mBtnRecord.setText(R.string.start);
        } else {
            mSurface.startRecording();
            mBtnRecord.setText(R.string.stop);
        }

        mRecording = !mRecording;
    }
}
