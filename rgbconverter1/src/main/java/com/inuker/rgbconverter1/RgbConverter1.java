package com.inuker.rgbconverter1;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;

import com.inuker.library.BaseApplication;
import com.inuker.library.EglCore;
import com.inuker.library.GlUtil;
import com.inuker.library.LogUtils;
import com.inuker.library.WindowSurface;
import com.inuker.library.YUVProgram;


import java.nio.ByteBuffer;

import static com.inuker.library.EglCore.FLAG_TRY_GLES3;

/**
 * Created by liwentian on 17/8/21.
 */

public class RgbConverter1 extends RgbConverter {

    public RgbConverter1(Context context) {
        super(context);
    }

    @Override
    void onDrawFrame() {
        readPixels();
        pixelsToBitmap();
    }

    @Override
    void onFrameAvailable() {

    }

    @Override
    void onDestroy() {

    }

    @Override
    void onStart() {

    }
}
