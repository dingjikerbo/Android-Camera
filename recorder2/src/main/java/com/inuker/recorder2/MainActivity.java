package com.inuker.recorder2;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CameraSurfaceView surfaceView = new CameraSurfaceView(this);
        setContentView(surfaceView);
    }
}
