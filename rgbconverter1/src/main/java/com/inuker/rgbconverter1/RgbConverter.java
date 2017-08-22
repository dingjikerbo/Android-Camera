package com.inuker.rgbconverter1;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Telephony;
import android.support.annotation.CallSuper;
import android.view.Surface;

import com.inuker.library.EglCore;
import com.inuker.library.EventDispatcher;
import com.inuker.library.EventListener;
import com.inuker.library.LogUtils;
import com.inuker.library.WindowSurface;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.inuker.library.EglCore.FLAG_TRY_GLES3;

/**
 * Created by liwentian on 17/8/21.
 */

public abstract class RgbConverter implements IRgbConverter, Handler.Callback, EventListener {

    final String TAG = getClass().getSimpleName();

    private static final int MSG_RENDER_CREATE = 1;
    private static final int MSG_FRAME_AVAILABLE = 2;
    private static final int MSG_RENDER_DESTROY = 3;

    protected HandlerThread mRenderThread;
    protected Handler mRenderHandler;

    protected Context mContext;

    protected int mWidth, mHeight;

    protected ByteBuffer mYUVBuffer;

    protected Surface mSurface;

    private EglCore mEglCore;

    protected WindowSurface mWindowSurface;

    RgbConverter(Context context, int width, int height, Surface surface) {
        LogUtils.v(String.format("New RgbConverter, width = %d, height = %d", width, height));
        mContext = context;

        mWidth = width;
        mHeight = height;

        mSurface = surface;
    }

    @CallSuper
    void onRenderCreate() {
        LogUtils.v(String.format("%s onRenderCreate", TAG));

        mYUVBuffer = ByteBuffer.allocateDirect(mWidth * mHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8)
                .order(ByteOrder.nativeOrder());

        mEglCore = new EglCore(null, FLAG_TRY_GLES3);

        if (mSurface != null) {
            mWindowSurface = new WindowSurface(mEglCore, mSurface, false);
        } else {
            int[] values = new int[1];
            GLES30.glGenTextures(1, values, 0);
            mWindowSurface = new WindowSurface(mEglCore, new SurfaceTexture(values[0]));
        }

        mWindowSurface.makeCurrent();

        EventDispatcher.observe(Events.FRAME_AVAILABLE, this);
    }

    @CallSuper
    void onFrameAvailable() {
        LogUtils.v(String.format("%s onFrameAvailable", TAG));
    }

    @CallSuper
    void onRenderDestroy() {
        LogUtils.v(String.format("%s onRenderDestroy", TAG));
        EventDispatcher.unObserve(Events.FRAME_AVAILABLE, this);
    }

    @CallSuper
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_RENDER_CREATE:
                onRenderCreate();
                break;

            case MSG_FRAME_AVAILABLE:
                onFrameAvailable();
                break;

            case MSG_RENDER_DESTROY:
                onRenderDestroy();
                break;
        }
        return false;
    }

    @Override
    public void start() {
        if (mRenderThread != null) {
            throw new IllegalStateException();
        }

        mRenderThread = new HandlerThread(TAG);
        mRenderThread.start();

        mRenderHandler = new Handler(mRenderThread.getLooper(), this);
        mRenderHandler.sendEmptyMessage(MSG_RENDER_CREATE);
    }

    @Override
    public void destroy() {
        mRenderHandler.sendEmptyMessage(MSG_RENDER_DESTROY);
        mRenderHandler.getLooper().quitSafely();
        mRenderHandler = null;
        mRenderThread = null;
    }

    @Override
    public void frameAvailable(byte[] bytes) {
        synchronized (mYUVBuffer) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(bytes);
        }
        mRenderHandler.sendEmptyMessage(MSG_FRAME_AVAILABLE);
    }

    @Override
    public void onEvent(int event, Object object) {
        switch (event) {
            case Events.FRAME_AVAILABLE:
                frameAvailable((byte[]) object);
                break;
        }
    }

    public static RgbConverter loadConverter(Class<? extends RgbConverter> clazz, Context context,
                                             int width, int height, Surface surface) {
        try {
            Constructor constructor = clazz.getConstructor(Context.class, int.class, int.class, Surface.class);
            constructor.setAccessible(true);
            return (RgbConverter) constructor.newInstance(context, width, height, surface);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
