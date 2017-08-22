package com.inuker.rgbconverter1;

import android.app.Activity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.inuker.library.BaseActivity;
import com.inuker.library.BaseApplication;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final int WIDTH = BaseApplication.getScreenWidth();
    private static final int HEIGHT = BaseApplication.getScreenHeight();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FrameLayout full = (FrameLayout) findViewById(R.id.full);
        CameraSurfaceView fullSurface = new CameraSurfaceView(this);
        full.addView(fullSurface);

        addMiniSurface(R.id.mini1, RgbConverter1.class);
        addMiniSurface(R.id.mini2, RgbConverter2.class);
    }

    private void addMiniSurface(int id, Class clazz) {
        final FrameLayout mini = (FrameLayout) findViewById(id);
        MiniSurfaceView miniSurface = new MiniSurfaceView(this, clazz);
        miniSurface.setZOrderOnTop(true);
        mini.addView(miniSurface);
    }
}
