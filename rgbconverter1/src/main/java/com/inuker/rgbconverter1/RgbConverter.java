package com.inuker.rgbconverter1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Telephony;
import android.support.annotation.CallSuper;
import android.view.Surface;

import com.inuker.library.BaseApplication;
import com.inuker.library.EglCore;
import com.inuker.library.EventDispatcher;
import com.inuker.library.EventListener;
import com.inuker.library.GlUtil;
import com.inuker.library.LogUtils;
import com.inuker.library.RuntimeCounter;
import com.inuker.library.TaskUtils;
import com.inuker.library.WindowSurface;
import com.inuker.library.YUVProgram;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.inuker.library.EglCore.FLAG_TRY_GLES3;

/**
 * Created by liwentian on 17/8/21.
 */

public abstract class RgbConverter implements IRgbConverter {

    final String TAG = getClass().getSimpleName();

    protected ByteBuffer mYUVBuffer;

    protected ByteBuffer mPixelBuffer;

    protected Context mContext;

    protected int mWidth, mHeight;

    protected RuntimeCounter mRuntimeCounter;

    public RgbConverter(Context context) {
        mContext = context;

        mWidth = BaseApplication.getScreenWidth();
        mHeight = BaseApplication.getScreenHeight();

        mYUVBuffer = ByteBuffer.allocateDirect(mWidth * mHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8)
                .order(ByteOrder.nativeOrder());

        mPixelBuffer = ByteBuffer.allocate(mWidth * mHeight * 4);

        mRuntimeCounter = new RuntimeCounter();
    }

    @Override
    public final void start() {
        onStart();
        GlUtil.checkGlError(String.format("%s start", TAG));
    }

    @Override
    public final void destroy() {
        mYUVBuffer = null;
        mPixelBuffer = null;
        System.gc();
        onDestroy();
    }

    @Override
    public final void frameDrawed() {
        onDrawFrame();
        GlUtil.checkGlError(String.format("%s frameDrawed", TAG));
    }

    @Override
    public final void frameAvailable(byte[] bytes) {
        synchronized (mYUVBuffer) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(bytes);
        }
    }

    void readPixels() {
        long start = System.currentTimeMillis();
        mPixelBuffer.position(0);
        GLES30.glReadPixels(0, 0, mWidth, mHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, mPixelBuffer);
        mRuntimeCounter.add(System.currentTimeMillis() - start);
        LogUtils.v(String.format("%s glReadPixels takes %dms", TAG, System.currentTimeMillis() - start));
        EventDispatcher.dispatch(Events.FPS_AVAILABLE, mRuntimeCounter.getAvg());
    }

    void pixelsToBitmap() {
        pixelsToBitmap(mPixelBuffer);
    }

    private boolean ENABLE = false;

    void pixelsToBitmap(ByteBuffer pixelBuffer) {
        if (!ENABLE) {
            return;
        }

        long start = System.currentTimeMillis();

        final Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        pixelBuffer.rewind();
        bmp.copyPixelsFromBuffer(pixelBuffer);

        EventDispatcher.dispatch(Events.BITMAP_AVAILABLE, bmp);
        LogUtils.v(String.format("pixelsToBitmap takes %dms", System.currentTimeMillis() - start));
    }

    abstract void onStart();

    abstract void onDrawFrame();

    abstract void onDestroy();
}
