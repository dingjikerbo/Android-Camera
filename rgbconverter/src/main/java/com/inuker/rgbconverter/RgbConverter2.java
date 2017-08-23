package com.inuker.rgbconverter;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.inuker.library.EglCore;
import com.inuker.library.OffscreenSurface;
import com.inuker.library.YUVProgram;

/**
 * Created by liwentian on 17/8/21.
 */

public class RgbConverter2 extends SingleRgbConverter {

    private YUVProgram mYUVProgram;

    public RgbConverter2(Context context) {
        super(context);
    }

    @Override
    void onSurfaceCreated() {
        super.onSurfaceCreated();

        mYUVProgram = new YUVProgram(mContext, mWidth, mHeight);
    }

    @Override
    void onSurfaceDestroy() {
        mYUVProgram.release();
        super.onSurfaceDestroy();
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
        pixelsToBitmap();
    }
}
