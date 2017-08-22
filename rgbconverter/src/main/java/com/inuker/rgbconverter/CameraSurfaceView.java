package com.inuker.rgbconverter;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES30;
import android.os.Message;
import android.view.SurfaceHolder;

import com.inuker.library.BaseApplication;
import com.inuker.library.BaseSurfaceView;
import com.inuker.library.EglCore;
import com.inuker.library.WindowSurface;
import com.inuker.library.YUVProgram;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.opengl.GLES20.glGenTextures;

/**
 * Created by liwentian on 17/8/16.
 */

public class CameraSurfaceView extends BaseSurfaceView implements Camera.PreviewCallback {

    private static final int MSG_DRAW_FRAME = 4;

    private Camera mCamera;

    private YUVProgram mYUVProgram;
    private ByteBuffer mYUVBuffer;

    private EglCore mEglCore;
    private WindowSurface mWindowSurface;

    private SurfaceTexture mSurfaceTexture;

    private RgbConverter mRgbConverter;

    public CameraSurfaceView(Context context, RgbConverter converter) {
        super(context);

        mRgbConverter = converter;
    }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open(1);

        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        mWindowSurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mWindowSurface.makeCurrent();

        mYUVProgram = new YUVProgram(getContext(), BaseApplication.getScreenWidth(), BaseApplication.getScreenHeight());
        mYUVProgram.setUpsideDown();

        mRgbConverter.start();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
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

        mYUVBuffer = null;
        mRgbConverter.destroy();

        mSurfaceTexture.release();

        mYUVProgram.release();
        mWindowSurface.release();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }

    private void onDrawFrame() {
        GLES30.glClearColor(1.0f, 0.2f, 0.2f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mSurfaceTexture.updateTexImage();

        mWindowSurface.makeCurrent();

        mYUVProgram.useProgram();
        synchronized (mYUVBuffer) {
            mYUVProgram.setUniforms(mYUVBuffer.array());
        }
        mYUVProgram.draw();

        mWindowSurface.swapBuffers();

        mRgbConverter.frameDrawed();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (mYUVBuffer) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(data);
        }

        mRgbConverter.frameAvailable(data);

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
