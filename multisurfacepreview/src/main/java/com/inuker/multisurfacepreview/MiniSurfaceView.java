package com.inuker.multisurfacepreview;

import android.content.Context;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

import com.inuker.library.BaseSurfaceView;
import com.inuker.library.EglCore;
import com.inuker.library.EventDispatcher;
import com.inuker.library.EventListener;
import com.inuker.library.WindowSurface;
import com.inuker.library.program.TextureProgram;
import com.inuker.library.utils.GlUtil;
import com.inuker.library.utils.LogUtils;

import static com.inuker.multisurfacepreview.Events.EVENTS_DRAW;

/**
 * Created by dingjikerbo on 17/8/17.
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

        EventDispatcher.observe(EVENTS_DRAW, this);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mTextureProgram = new TextureProgram(getContext(), R.raw.tex_vertex, R.raw.filter_tex_fragment, width, height);
        GlUtil.checkGlError("surfaceChanged");
    }

    @Override
    public void onSurfaceDestroyed() {
        EventDispatcher.unObserve(EVENTS_DRAW, this);

        mTextureProgram.release();
        mWindowSurface.release();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }

    private void onDrawFrame(int offscreenTexture) {
//        LogUtils.v(String.format("%s onDrawFrame %d", getClass().getSimpleName(), offscreenTexture));
        mTextureProgram.draw(offscreenTexture);
        mWindowSurface.swapBuffers();

        GlUtil.checkGlError("onDrawFrame");
    }

    @Override
    public void onEvent(int event, Object object) {
        if (mRenderHandler != null) {
            mRenderHandler.obtainMessage(2, object).sendToTarget();
        }
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
