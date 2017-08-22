package com.inuker.rgbconverter1;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;

import com.inuker.library.EglCore;
import com.inuker.library.GlUtil;
import com.inuker.library.WindowSurface;
import com.inuker.library.YUVProgram;


import static com.inuker.library.EglCore.FLAG_TRY_GLES3;

/**
 * Created by liwentian on 17/8/21.
 */

public class RgbConverter1 extends RgbConverter {

    private YUVProgram mYUVProgram;

    public RgbConverter1(Context context, int width, int height, Surface surface) {
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
