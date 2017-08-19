package com.inuker.library;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by liwentian on 17/8/16.
 */

public abstract class BaseSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Handler.Callback {

    private static final int MSG_SURFACE_CREATED = 0x1001;
    private static final int MSG_SURFACE_CHANGED = 0x1002;
    private static final int MSG_SURFACE_DESTROY = 0x1003;

    protected HandlerThread mRenderThread;
    protected Handler mRenderHandler;

    protected Queue<Runnable> mPendingRunnables;

    private volatile boolean mSurfaceCreated;

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

    void init() {
        getHolder().addCallback(this);

        mPendingRunnables = new LinkedList<>();

        mRenderThread = new HandlerThread(getClass().getSimpleName());
        mRenderThread.start();

        mRenderHandler = new Handler(mRenderThread.getLooper(), this);
    }

    @Override
    public final void surfaceCreated(SurfaceHolder holder) {
        LogUtils.v(String.format("%s surfaceCreated", getName()));
        mRenderHandler.obtainMessage(MSG_SURFACE_CREATED, holder).sendToTarget();
    }

    abstract public void onSurfaceCreated(SurfaceHolder holder);

    @Override
    public final void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtils.v(String.format("%s surfaceChanged width = %d, height = %d", getName(), width, height));
        mRenderHandler.obtainMessage(MSG_SURFACE_CHANGED, width, height).sendToTarget();
    }

    abstract public void onSurfaceChanged(int width, int height);

    @Override
    public final void surfaceDestroyed(SurfaceHolder holder) {
        LogUtils.v(String.format("%s surfaceDestroyed", getName()));
        mRenderHandler.obtainMessage(MSG_SURFACE_DESTROY).sendToTarget();
        mRenderHandler.getLooper().quitSafely();
        mRenderHandler = null;
    }

    abstract public void onSurfaceDestroyed();

    @CallSuper
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SURFACE_CREATED:
                onSurfaceCreated((SurfaceHolder) msg.obj);
                mSurfaceCreated = true;
                dispatchPendingRunnables();
                break;

            case MSG_SURFACE_CHANGED:
                onSurfaceChanged(msg.arg1, msg.arg2);
                break;

            case MSG_SURFACE_DESTROY:
                onSurfaceDestroyed();
                mPendingRunnables.clear();
                mSurfaceCreated = false;
                break;
        }

        return false;
    }

    private void dispatchPendingRunnables() {
        synchronized (mPendingRunnables) {
            while (!mPendingRunnables.isEmpty()) {
                queueEvent(mPendingRunnables.poll());
            }
        }
    }

    public void queueEvent(Runnable runnable) {
        if (mRenderHandler == null || !mSurfaceCreated) {
            synchronized (mPendingRunnables) {
                mPendingRunnables.offer(runnable);
            }
            return;
        }

        mRenderHandler.post(runnable);
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
