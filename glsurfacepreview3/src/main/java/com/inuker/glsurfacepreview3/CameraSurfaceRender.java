package com.inuker.glsurfacepreview3;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.inuker.library.EglCore;
import com.inuker.library.LogUtils;
import com.inuker.library.OffscreenSurface;
import com.inuker.library.WindowSurface;
import com.inuker.library.YUVProgram;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Created by liwentian on 17/8/16.
 */

public class CameraSurfaceRender implements GLSurfaceView.Renderer, Camera.PreviewCallback {

    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;

    private YUVProgram mYUVProgram;

    private ByteBuffer mYUVBuffer;

    private GLSurfaceView mGLSurfaceView;

    private EGLSurface mOffscreenEglSurface;
    private EGLSurface mDisplayEglSurface;

    private EglCore mEglCore;

    public CameraSurfaceRender(GLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        LogUtils.v("onSurfaceCreated");
        mCamera = Camera.open(1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        LogUtils.v(String.format("onSurfaceChanged width = %d, height = %d", width, height));
        
        int bufferSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;

        mYUVBuffer = ByteBuffer.allocateDirect(bufferSize)
                .order(ByteOrder.nativeOrder());

        int[] textures = new int[1];
        glGenTextures(1, textures, 0);
        mSurfaceTexture = new SurfaceTexture(textures[0]);

        mYUVProgram = new YUVProgram(mGLSurfaceView.getContext(), width, height);

        mEglCore = new EglCore(EGL14.eglGetCurrentContext(), EglCore.FLAG_TRY_GLES3);

        mOffscreenEglSurface = mEglCore.createOffscreenSurface(width, height);
        mDisplayEglSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);

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
    public void onDrawFrame(GL10 gl) {
        mEglCore.makeCurrent(mOffscreenEglSurface);

        mYUVProgram.useProgram();
        synchronized (mYUVBuffer) {
            mYUVProgram.setUniforms(mYUVBuffer.array());
        }
        mYUVProgram.draw();

        mEglCore.makeCurrent(mDisplayEglSurface, mOffscreenEglSurface);

        GLES30.glBlitFramebuffer(0, 0, 1920, 1080, 0, 0, 1920, 1080, GL_COLOR_BUFFER_BIT, GL_NEAREST);
        mEglCore.swapBuffers(mDisplayEglSurface);

        mSurfaceTexture.updateTexImage();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (mYUVBuffer) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(data);
        }

        mGLSurfaceView.requestRender();
        mCamera.addCallbackBuffer(data);
    }
}
