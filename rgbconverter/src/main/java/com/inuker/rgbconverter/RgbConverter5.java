package com.inuker.rgbconverter;

import android.content.Context;
import android.opengl.GLES30;

import com.inuker.library.EventDispatcher;
import com.inuker.library.utils.GlUtil;
import com.inuker.library.utils.LogUtils;
import com.inuker.library.utils.NativeUtils;
import com.inuker.library.program.YUVProgram;

import java.nio.ByteBuffer;

/**
 * Created by liwentian on 17/8/22.
 */

public class RgbConverter5 extends SingleRgbConverter {

    private YUVProgram mYUVProgram;

    private int mPBO;

    public RgbConverter5(Context context) {
        super(context);
    }

    private void preparePBOBuffer() {
        int[] values = new int[1];
        GLES30.glGenBuffers(1, values, 0);
        mPBO = values[0];

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPBO);
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, mWidth * mHeight * 4, null, GLES30.GL_STREAM_READ);
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
    void onSurfaceCreated() {
        super.onSurfaceCreated();

        mYUVProgram = new YUVProgram(mContext, mWidth, mHeight);
        preparePBOBuffer();
    }

    @Override
    void onDrawSurface() {
        super.onDrawSurface();

        synchronized (mYUVBuffer) {
            mYUVProgram.useProgram();
            mYUVProgram.setUniforms(mYUVBuffer.array());
            mYUVProgram.draw();
        }

        readPixels();

        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER);
    }

    @Override
    void onSurfaceDestroy() {
        mYUVProgram.release();
        GLES30.glDeleteBuffers(1, new int[] {mPBO}, 0);
        GlUtil.checkGlError("glDestroy");

        super.onSurfaceDestroy();
    }
}
