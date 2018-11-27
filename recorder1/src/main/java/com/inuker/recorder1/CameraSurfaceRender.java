package com.inuker.recorder1;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import com.inuker.library.utils.CameraHelper;
import com.inuker.library.utils.LogUtils;
import com.inuker.library.encoder.BaseMovieEncoder;
import com.inuker.library.encoder.MovieEncoder1;
import com.inuker.library.program.YUVProgram;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGenTextures;

/**
 * Created by liwentian on 17/8/16.
 */

public class CameraSurfaceRender implements GLSurfaceView.Renderer, Camera.PreviewCallback {

    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;

    private YUVProgram mYUVProgram;

    private ByteBuffer mYUVBuffer;

    private GLSurfaceView mGLSurfaceView;

    private BaseMovieEncoder mVideoEncoder;

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

        mVideoEncoder = new MovieEncoder1(mGLSurfaceView.getContext(), width, height);

        int bufferSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;

        mYUVBuffer = ByteBuffer.allocateDirect(bufferSize)
                .order(ByteOrder.nativeOrder());

        int[] textures = new int[1];
        glGenTextures(1, textures, 0);
        mSurfaceTexture = new SurfaceTexture(textures[0]);

        mYUVProgram = new YUVProgram(mGLSurfaceView.getContext(), width, height);

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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(1f, 1f, 1f, 1f);

        mYUVProgram.useProgram();
        synchronized (mYUVBuffer) {
            mYUVProgram.setUniforms(mYUVBuffer.array());
        }
        mYUVProgram.draw();

        mSurfaceTexture.updateTexImage();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (mYUVBuffer) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(data);
        }

        mVideoEncoder.frameAvailable(data, mSurfaceTexture.getTimestamp());

        mGLSurfaceView.requestRender();
        mCamera.addCallbackBuffer(data);
    }

    public void startRecording() {
        if (!mVideoEncoder.isRecording()) {
            File output = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO, "");
            mVideoEncoder.startRecording(new BaseMovieEncoder.EncoderConfig(output, null));
        }
    }

    public void stopRecording() {
        if (mVideoEncoder.isRecording()) {
            mVideoEncoder.stopRecording();
        }
    }
}
