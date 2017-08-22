package com.inuker.rgbconverter;

import android.content.Context;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.inuker.library.EglCore;
import com.inuker.library.EventDispatcher;
import com.inuker.library.GlUtil;
import com.inuker.library.LogUtils;
import com.inuker.library.NativeUtils;
import com.inuker.library.OffscreenSurface;
import com.inuker.library.YUVProgram;

import java.nio.ByteBuffer;

/**
 * Created by liwentian on 17/8/22.
 */

public class RgbConverter5 extends RgbConverter implements Handler.Callback {

    private static final int MSG_SURFACE_CREATE = 1;
    private static final int MSG_DRAW_FRAME = 2;
    private static final int MSG_SURFACE_DESTROY = 3;

    private HandlerThread mRenderThread;
    private Handler mRenderHandler;

    private YUVProgram mYUVProgram;

    private int mFrameBuffer;

    private int mOffscreenTexture;

    private EglCore mEglCore;
    private OffscreenSurface mOffscreenSurface;

    private int mPBO;

    public RgbConverter5(Context context) {
        super(context);
    }

    @Override
    void onStart() {
        mRenderThread = new HandlerThread(TAG);
        mRenderThread.start();
        mRenderHandler = new Handler(mRenderThread.getLooper(), this);
        mRenderHandler.sendEmptyMessage(MSG_SURFACE_CREATE);
    }

    private void prepareFramebuffer() {
        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES30.glGenTextures(1, values, 0);
        mOffscreenTexture = values[0];   // expected > 0
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mOffscreenTexture);

        // Create texture storage.
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, mWidth, mHeight, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);

        // Set parameters.  We're probably using non-power-of-two dimensions, so
        // some values may not be available for use.
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_NEAREST);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,
                GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,
                GLES30.GL_CLAMP_TO_EDGE);

        // Create framebuffer object and bind it.
        GLES30.glGenFramebuffers(1, values, 0);
        mFrameBuffer = values[0];    // expected > 0
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffer);

        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, mOffscreenTexture, 0);

        // See if GLES is happy with all this.
        int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        // Switch back to the default framebuffer.
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        GlUtil.checkGlError("prepareFramebuffer done");
    }

    private void preparePBOBuffer() {
        int[] values = new int[1];
        GLES30.glGenBuffers(1, values, 0);
        mPBO = values[0];

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPBO);
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, mWidth * mHeight * 4, null, GLES30.GL_STREAM_READ);
    }

    @Override
    void onDrawFrame() {
        mRenderHandler.sendEmptyMessage(MSG_DRAW_FRAME);
    }

    @Override
    void readPixels() {
        long start1 = System.currentTimeMillis();

        NativeUtils.glReadPixels(0, 0, mWidth, mHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE);
        GlUtil.checkGlError("glReadPixels");

        LogUtils.v(String.format("glReadPixels takes %dms", System.currentTimeMillis() - start1));

        long start2 = System.currentTimeMillis();
        final ByteBuffer pixelBuffer = (ByteBuffer) GLES30.glMapBufferRange(GLES30.GL_PIXEL_PACK_BUFFER, 0, mWidth * mHeight * 4, GLES30.GL_MAP_READ_BIT);
        LogUtils.v(String.format("glMapBuffer takes %dms", System.currentTimeMillis() - start2));

        mRuntimeCounter.add(System.currentTimeMillis() - start1);

        pixelsToBitmap(pixelBuffer);

        GlUtil.checkGlError("glMapBufferRange");

        LogUtils.v(String.format("%s readPixels takes %dms", TAG, System.currentTimeMillis() - start1));
        EventDispatcher.dispatch(Events.FPS_AVAILABLE, mRuntimeCounter.getAvg());
    }

    @Override
    void onDestroy() {
        mRenderHandler.sendEmptyMessage(MSG_SURFACE_DESTROY);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SURFACE_CREATE:
                onSurfaceCreated();
                break;

            case MSG_DRAW_FRAME:
                onDrawSurface();
                break;
            case MSG_SURFACE_DESTROY:
                onSurfaceDestroy();
                break;
        }
        return false;
    }

    private void onSurfaceCreated() {
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        mOffscreenSurface = new OffscreenSurface(mEglCore, mWidth, mHeight);
        mOffscreenSurface.makeCurrent();

        mYUVProgram = new YUVProgram(mContext, mWidth, mHeight);
        mYUVProgram.setUpsideDown();

        prepareFramebuffer();
        preparePBOBuffer();
    }

    private void onDrawSurface() {
        synchronized (mYUVBuffer) {
            mYUVProgram.useProgram();
            mYUVProgram.setUniforms(mYUVBuffer.array());
            mYUVProgram.draw();
        }

        readPixels();
        pixelsToBitmap();

        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER);
    }

    private void onSurfaceDestroy() {
        mYUVProgram.release();
        GLES30.glDeleteFramebuffers(1, new int[] {mFrameBuffer}, 0);
        GLES30.glDeleteBuffers(1, new int[] {mPBO}, 0);
        GLES30.glDeleteTextures(1, new int[] {mOffscreenTexture}, 0);
        GlUtil.checkGlError("glDestroy");
        mOffscreenSurface.release();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }
}
