package com.inuker.multisurfacepreview;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.SurfaceHolder;

import com.inuker.library.BaseSurfaceView;
import com.inuker.library.EglCore;
import com.inuker.library.TextureProgram;
import com.inuker.library.WindowSurface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.opengl.GLES20.glGenTextures;

/**
 * Created by liwentian on 17/8/16.
 */

public class CameraSurfaceView extends BaseSurfaceView implements Camera.PreviewCallback {

    private static final int MSG_SURFACE_CREATED = 1;
    private static final int MSG_SURFACE_CHANGED = 2;
    private static final int MSG_SURFACE_DESTROY = 3;
    private static final int MSG_DRAW_FRAME = 4;

    private Camera mCamera;

    private TextureProgram mTextureProgram;
    private ByteBuffer mYUVBuffer;

    private EglCore mEglCore;
    private WindowSurface mWindowSurface;

    private SurfaceTexture mSurfaceTexture;

    public CameraSurfaceView(Context context) {
        super(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);

        mCamera = Camera.open(1);
        mRenderHandler.obtainMessage(MSG_SURFACE_CREATED, holder).sendToTarget();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        mRenderHandler.obtainMessage(MSG_SURFACE_CHANGED, width, height).sendToTarget();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }

        mRenderHandler.obtainMessage(MSG_SURFACE_DESTROY).sendToTarget();
    }

    private void doSurfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        mWindowSurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mWindowSurface.makeCurrent();
    }

    private void doSurfaceChanged(int width, int height) {
        mTextureProgram = new TextureProgram(getContext(), width, height);

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

    private void doSurfaceDestroyed() {
        mWindowSurface.release();
        mTextureProgram.release();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }

    private void onDrawFrame() {
        GLES30.glClearColor(1.0f, 0.2f, 0.2f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mTextureProgram.useProgram();
        synchronized (mYUVBuffer) {
            mTextureProgram.setUniforms(mYUVBuffer.array());
        }
        mTextureProgram.draw();

        mWindowSurface.swapBuffers();

        mSurfaceTexture.updateTexImage();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (mYUVBuffer) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(data);
        }

        mRenderHandler.sendEmptyMessage(MSG_DRAW_FRAME);

        mCamera.addCallbackBuffer(data);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SURFACE_CREATED:
                doSurfaceCreated((SurfaceHolder) msg.obj);
                break;

            case MSG_SURFACE_CHANGED:
                doSurfaceChanged(msg.arg1, msg.arg2);
                break;

            case MSG_SURFACE_DESTROY:
                doSurfaceDestroyed();
                break;

            case MSG_DRAW_FRAME:
                onDrawFrame();
                break;
        }

        return false;
    }


}
