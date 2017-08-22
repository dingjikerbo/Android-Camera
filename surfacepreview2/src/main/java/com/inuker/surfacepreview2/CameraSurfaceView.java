package com.inuker.surfacepreview2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.SurfaceHolder;

import com.inuker.library.BaseSurfaceView;
import com.inuker.library.EglCore;
import com.inuker.library.OffscreenSurface;
import com.inuker.library.WindowSurface;
import com.inuker.library.YUVProgram;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGenTextures;

/**
 * Created by liwentian on 17/8/16.
 */

public class CameraSurfaceView extends BaseSurfaceView implements Camera.PreviewCallback {

    private static final int MSG_DRAW_FRAME = 4;

    private Camera mCamera;

    private YUVProgram mTextureProgram;
    private ByteBuffer mYUVBuffer;

    private EglCore mEglCore;
    private WindowSurface mWindowSurface;

    private OffscreenSurface mOffscreenSurface;

    private SurfaceTexture mSurfaceTexture;

    public CameraSurfaceView(Context context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open(1);
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        mWindowSurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mWindowSurface.makeCurrent();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mTextureProgram = new YUVProgram(getContext(), width, height);

        mOffscreenSurface = new OffscreenSurface(mEglCore, width, height);

        int bufferSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;

        mYUVBuffer = ByteBuffer.allocateDirect(bufferSize)
                .order(ByteOrder.nativeOrder());

        int[] textures = new int[1];
        glGenTextures(1, textures, 0);
        mSurfaceTexture = new SurfaceTexture(textures[0]);

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.setPreviewCallbackWithBuffer(this);
        for (int i = 0; i < 2; i++) {
            byte[] callbackBuffer = new byte[bufferSize];
            mCamera.addCallbackBuffer(callbackBuffer);
        }

        mCamera.startPreview();
    }

    @Override
    public void onSurfaceDestroyed() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }

        mTextureProgram.release();
        mWindowSurface.release();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }

    private void onDrawFrame() {
        mOffscreenSurface.makeCurrent();

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        mTextureProgram.useProgram();
        synchronized (mYUVBuffer) {
            mTextureProgram.setUniforms(mYUVBuffer.array());
        }
        mTextureProgram.draw();

        mWindowSurface.makeCurrentReadFrom(mOffscreenSurface);

        GLES30.glBlitFramebuffer(0, 0, mWindowSurface.getWidth(), mWindowSurface.getHeight(),
                0, 0, mOffscreenSurface.getWidth(), mOffscreenSurface.getHeight(), GL_COLOR_BUFFER_BIT, GL_NEAREST);

        mWindowSurface.swapBuffers();

        mSurfaceTexture.updateTexImage();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (mYUVBuffer) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(data);
        }

        if (mRenderHandler != null) {
            mRenderHandler.removeMessages(MSG_DRAW_FRAME);
            mRenderHandler.sendEmptyMessage(MSG_DRAW_FRAME);
        }

        mCamera.addCallbackBuffer(data);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_DRAW_FRAME:
                onDrawFrame();
                break;
        }

        return super.handleMessage(msg);
    }
}
