package com.inuker.recorder2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.os.Message;
import android.view.SurfaceHolder;

import com.inuker.library.BaseSurfaceView;
import com.inuker.library.CameraHelper;
import com.inuker.library.EglCore;
import com.inuker.library.BaseMovieEncoder;
import com.inuker.library.MovieEncoder2;
import com.inuker.library.TextureProgram;
import com.inuker.library.WindowSurface;
import com.inuker.library.YUVProgram;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCheckFramebufferStatus;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Created by liwentian on 17/8/16.
 */

public class CameraSurfaceView extends BaseSurfaceView implements Camera.PreviewCallback {

    private static final int MSG_DRAW_FRAME = 4;

    private Camera mCamera;

    private YUVProgram mYUVProgram;
    private TextureProgram mTextureProgram;
    private ByteBuffer mYUVBuffer;

    private EglCore mEglCore;
    private WindowSurface mWindowSurface;

    private int mOffscreenTexture;

    private int mFramebuffer;

    private SurfaceTexture mSurfaceTexture;

    private FaceRender mFaceRender;

    private MovieEncoder2 mVideoEncoder;

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

    private void prepareFrameBuffer(int width, int height) {
        int[] values = new int[1];

        glGenTextures(1, values, 0);
        mOffscreenTexture = values[0];   // expected > 0
        glBindTexture(GL_TEXTURE_2D, mOffscreenTexture);

        // Create texture storage.
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, null);

        // Set parameters.  We're probably using non-power-of-two dimensions, so
        // some values may not be available for use.
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
                GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                GL_CLAMP_TO_EDGE);

        // Create framebuffer object and bind it.
        glGenFramebuffers(1, values, 0);
        mFramebuffer = values[0];    // expected > 0
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebuffer);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, mOffscreenTexture, 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        prepareFrameBuffer(width, height);

        mFaceRender = new FaceRender(getContext());
        mYUVProgram = new YUVProgram(getContext(), width, height);
        mTextureProgram = new TextureProgram(getContext(), width, height);
        mVideoEncoder = new MovieEncoder2(getContext(), width, height);

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

        mYUVProgram.release();
        mTextureProgram.release();
        mWindowSurface.release();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }

    private void onDrawFrame() {
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebuffer);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        mYUVProgram.useProgram();
        synchronized (mYUVBuffer) {
            mYUVProgram.setUniforms(mYUVBuffer.array());
        }
        mYUVProgram.draw();

        mFaceRender.draw();


        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        mTextureProgram.draw(mOffscreenTexture);
        mVideoEncoder.frameAvailable(mOffscreenTexture, mSurfaceTexture.getTimestamp());

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

    public void startRecording() {
        mRenderHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mVideoEncoder.isRecording()) {
                    File output = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO, "");
                    mVideoEncoder.startRecording(new BaseMovieEncoder.EncoderConfig(output, EGL14.eglGetCurrentContext()));
                }
            }
        });
    }

    public void stopRecording() {
        mRenderHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoEncoder.isRecording()) {
                    mVideoEncoder.stopRecording();
                }
            }
        });
    }
}
