package com.inuker.recorder3;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    public static final String TAG = "bush";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new CameraGLSurfaceView(this));
    }
}
