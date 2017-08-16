package com.inuker.library;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by liwentian on 17/8/16.
 */

public abstract class BaseSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Handler.Callback {

    protected HandlerThread mRenderThread;
    protected Handler mRenderHandler;

    public BaseSurfaceView(Context context) {
        super(context);
        init();
    }

    public BaseSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @CallSuper
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.v("surfaceCreated");
    }

    @CallSuper
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtils.v(String.format("surfaceChanged width = %d, height = %d", width, height));
    }

    @CallSuper
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtils.v("surfaceDestroyed");
    }

    void init() {
        getHolder().addCallback(this);

        mRenderThread = new HandlerThread(getClass().getSimpleName());
        mRenderThread.start();

        mRenderHandler = new Handler(mRenderThread.getLooper(), this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    public void queueEvent(Runnable runnable) {
        mRenderHandler.post(runnable);
    }
}
