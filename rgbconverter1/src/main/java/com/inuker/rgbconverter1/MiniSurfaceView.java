package com.inuker.rgbconverter1;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.inuker.library.BaseApplication;
import com.inuker.library.BaseSurfaceView;

/**
 * Created by liwentian on 17/8/22.
 */

public class MiniSurfaceView extends BaseSurfaceView {

    private RgbConverter mRgbConverter;

    private Class mConverterClazz;

    public MiniSurfaceView(Context context, Class converter) {
        super(context);
        getHolder().addCallback(this);
        mConverterClazz = converter;
    }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder) {
        mRgbConverter = RgbConverter.loadConverter(mConverterClazz, getContext(),
                BaseApplication.getScreenWidth(), BaseApplication.getScreenHeight(),
                getHolder().getSurface()
        );
        mRgbConverter.start();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }

    @Override
    public void onSurfaceDestroyed() {

    }
}
