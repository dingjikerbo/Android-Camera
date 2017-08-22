package com.inuker.rgbconverter1;

import android.content.Context;
import android.view.Surface;

import com.inuker.library.GlUtil;
import com.inuker.library.YUVProgram;

/**
 * Created by liwentian on 17/8/21.
 */

public class RgbConverter2 extends RgbConverter {

    private YUVProgram mYUVProgram;

    public RgbConverter2(Context context, int width, int height, Surface surface) {
        super(context, width, height, surface);
    }

    @Override
    void onRenderCreate() {
        super.onRenderCreate();

        mYUVProgram = new YUVProgram(mContext, mWidth, mHeight);

        GlUtil.checkGlError("render create done");
    }

    @Override
    void onFrameAvailable() {
        super.onFrameAvailable();

        synchronized (mYUVBuffer) {
            mYUVProgram.useProgram();
            mYUVProgram.setUniforms(mYUVBuffer.array());
            mYUVProgram.draw();
        }

        mWindowSurface.swapBuffers();

        GlUtil.checkGlError("draw frame done");
    }
}
