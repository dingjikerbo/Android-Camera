package com.inuker.multisurfacepreview;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.GLES30;
import android.opengl.GLU;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

import com.inuker.library.BaseSurfaceView;
import com.inuker.library.EglCore;
import com.inuker.library.EventDispatcher;
import com.inuker.library.EventListener;
import com.inuker.library.GlUtil;
import com.inuker.library.LogUtils;
import com.inuker.library.OffscreenSurface;
import com.inuker.library.TextureProgram;
import com.inuker.library.WindowSurface;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_NEAREST;

/**
 * Created by liwentian on 17/8/17.
 */

public class MiniSurfaceView extends BaseSurfaceView implements Handler.Callback, EventListener {

    private EglCore mEglCore;

    private EGLContext mSharedEGLContext;

    private WindowSurface mWindowSurface;

    private TextureProgram mTextureProgram;

    public MiniSurfaceView(Context context, EGLContext sharedContext) {
        super(context);
        mSharedEGLContext = sharedContext;
        LogUtils.v(String.format("Mini %s", sharedContext));
    }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(mSharedEGLContext, EglCore.FLAG_TRY_GLES3);

        mWindowSurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mWindowSurface.makeCurrent();

        GlUtil.checkGlError("surfaceCreated");

        EventDispatcher.observe(1, this);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mTextureProgram = new TextureProgram(getContext(), R.raw.tex_vertex, R.raw.filter_tex_fragment, width, height);
        GlUtil.checkGlError("surfaceChanged");
    }

    @Override
    public void onSurfaceDestroyed() {

    }

    private void onDrawFrame(int offscreenTexture) {
//        LogUtils.v(String.format("%s onDrawFrame %d", getClass().getSimpleName(), offscreenTexture));
        mTextureProgram.draw(offscreenTexture);
        mWindowSurface.swapBuffers();

        GlUtil.checkGlError("onDrawFrame");
    }

    @Override
    public void onEvent(int event, Object object) {
        mRenderHandler.obtainMessage(2, object).sendToTarget();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                onDrawFrame((Integer) msg.obj);
                break;
        }
        return super.handleMessage(msg);
    }
}
