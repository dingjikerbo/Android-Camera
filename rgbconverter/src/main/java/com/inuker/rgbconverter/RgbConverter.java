package com.inuker.rgbconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.opengl.GLES30;

import com.inuker.library.BaseApplication;
import com.inuker.library.EventDispatcher;
import com.inuker.library.utils.GlUtil;
import com.inuker.library.utils.LogUtils;
import com.inuker.library.RuntimeCounter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        onDestroy();
        mYUVBuffer = null;
        mPixelBuffer = null;
        LogUtils.e("destroy, mPixelbuffer null");
        System.gc();
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
        if (mPixelBuffer == null) {
            LogUtils.e(String.format("readPixels failed, pixelBuffer already set null!!"));
            return;
        }
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

    void pixelsToBitmap(ByteBuffer pixelBuffer) {
        if (pixelBuffer == null) {
            LogUtils.e(String.format("pixelsToBitmap failed, pixelBuffer already set null!!"));
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
