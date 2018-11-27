package com.inuker.rgbconverter;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.CallSuper;

import com.inuker.library.EglCore;
import com.inuker.library.utils.LogUtils;
import com.inuker.library.OffscreenSurface;

/**
 * Created by liwentian on 17/8/22.
 */

public class SingleRgbConverter extends RgbConverter implements Handler.Callback {

    private static final int MSG_SURFACE_CREATE = 1;
    private static final int MSG_DRAW_FRAME = 2;
    private static final int MSG_SURFACE_DESTROY = 3;

    private HandlerThread mRenderThread;
    private Handler mRenderHandler;

    protected EglCore mEglCore;
    protected OffscreenSurface mOffscreenSurface;

    public SingleRgbConverter(Context context) {
        super(context);
    }

    @Override
    void onStart() {
        mRenderThread = new HandlerThread(TAG);
        mRenderThread.start();
        mRenderHandler = new Handler(mRenderThread.getLooper(), this);
        mRenderHandler.sendEmptyMessage(MSG_SURFACE_CREATE);
    }

    @Override
    void onDrawFrame() {
        if (mRenderHandler != null) {
            mRenderHandler.removeMessages(MSG_DRAW_FRAME);
            mRenderHandler.sendEmptyMessage(MSG_DRAW_FRAME);
        }
    }

    @Override
    void onDestroy() {
        if (mRenderHandler != null) {
            mRenderHandler.sendEmptyMessage(MSG_SURFACE_DESTROY);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SURFACE_CREATE:
                onSurfaceCreated();
                break;

            case MSG_DRAW_FRAME:
                if (mYUVBuffer != null) {
                    onDrawSurface();
                }
                break;
            case MSG_SURFACE_DESTROY:
                onSurfaceDestroy();
                break;
        }
        return false;
    }

    @CallSuper
    void onSurfaceCreated() {
        LogUtils.v(String.format("%s onSurfaceCreated", TAG));
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        mOffscreenSurface = new OffscreenSurface(mEglCore, mWidth, mHeight);
        mOffscreenSurface.makeCurrent();
    }

    @CallSuper
    void onDrawSurface() {
        LogUtils.v(String.format("%s onDrawSurface", TAG));
    }

    @CallSuper
    void onSurfaceDestroy() {
        LogUtils.v(String.format("%s onSurfaceDestroy", TAG));

        mRenderThread.quit();
        mRenderThread = null;
        mRenderHandler = null;

        mOffscreenSurface.release();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }
}
