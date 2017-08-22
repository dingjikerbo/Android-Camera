package com.inuker.rgbconverter1;

import android.content.Context;
import android.view.Surface;

import com.inuker.library.EglCore;
import com.inuker.library.GlUtil;
import com.inuker.library.OffscreenSurface;
import com.inuker.library.YUVProgram;

/**
 * Created by liwentian on 17/8/21.
 */

public class RgbConverter2 extends RgbConverter {

    private EglCore mEglCore;

    private OffscreenSurface mOffscreenSurface;

    private YUVProgram mYUVProgram;

    public RgbConverter2(Context context) {
        super(context);
    }

    @Override
    void onDestroy() {

    }

    @Override
    void onStart() {
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);

        mOffscreenSurface = new OffscreenSurface(mEglCore, mWidth, mHeight);
        mOffscreenSurface.makeCurrent();

        mYUVProgram = new YUVProgram(mContext, mWidth, mHeight);
    }

    @Override
    void onDrawFrame() {
        mOffscreenSurface.makeCurrent();

        synchronized (mYUVBuffer) {
            mYUVProgram.useProgram();
            mYUVProgram.setUniforms(mYUVBuffer.array());
            mYUVProgram.draw();
        }

        mOffscreenSurface.swapBuffers();

        readPixels();
    }

    @Override
    void onFrameAvailable() {
    }
}
