package com.inuker.multisurfacepreview;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;

public class MainActivity extends Activity {

    private CameraSurfaceView mCameraSurfaceView;

    private FilterSurfaceView mFilterSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera);
        mFilterSurfaceView = (FilterSurfaceView) findViewById(R.id.filter);
    }
}
