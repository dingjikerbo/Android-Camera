package com.inuker.rgbconverter;

import android.content.Context;

import com.inuker.library.program.YUVProgram;

/**
 * Created by dingjikerbo on 17/8/21.
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
            mYUVProgram.draw(mYUVBuffer.array());
        }

        readPixels();
        pixelsToBitmap();
    }
}
