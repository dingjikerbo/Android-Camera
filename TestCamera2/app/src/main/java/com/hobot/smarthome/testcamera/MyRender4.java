package com.hobot.smarthome.testcamera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by liwentian on 17/6/15.
 */

/**
 * 用OpenGL转NV21到RGB
 */
public class MyRender4 implements GLSurfaceView.Renderer, Camera.PreviewCallback {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private GLSurfaceView mSurfaceView;

    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;

    private ByteBuffer mYUVBuffer;

    private final Object mLock = new Object();

    private TextureProgram mTextureProgram;

    private RectProgram mRectProgram;
    private CircleProgram mCircleProgram;

    private volatile boolean mSurfaceCreated;

    public MyRender4(GLSurfaceView view) {
        mSurfaceView = view;

        mCamera = Camera.open(1);

        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(WIDTH, HEIGHT);
        params.setPreviewFpsRange(30000, 30000);
        mCamera.setParameters(params);

        mYUVBuffer = ByteBuffer.allocateDirect(WIDTH * HEIGHT * 3 / 2)
                .order(ByteOrder.nativeOrder());
    }

    private void startPreview() {
        if (mCamera == null) {
            return;
        }

        mCamera.stopPreview();

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurfaceTexture = new SurfaceTexture(textures[0]);

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.setPreviewCallbackWithBuffer(this);
        for (int i = 0; i < 2; i++) {
            byte[] callbackBuffer = new byte[WIDTH * HEIGHT * 3 / 2];
            mCamera.addCallbackBuffer(callbackBuffer);
        }

        mCamera.startPreview();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSurfaceCreated = true;

        GLES20.glClearColor(0, 0, 0, 1);

        Context context = mSurfaceView.getContext();
        mTextureProgram = new TextureProgram(context);
        mRectProgram = new RectProgram(context);
        mCircleProgram = new CircleProgram(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        startPreview();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.v("bush", "onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(1f, 1f, 1f, 1f);

        if (!mSurfaceCreated) {
            return;
        }

        synchronized (mLock) {
            mTextureProgram.useProgram();
            mTextureProgram.setUniforms(mYUVBuffer.array());
            mTextureProgram.draw();

            mRectProgram.useProgram();
            Face face = new Face(0, 0, 960, 1080, Color.RED);
            face.bindData(mRectProgram);
            face.draw();

            mCircleProgram.useProgram();
            Circle circle = new Circle(960, 540, 540, Color.GREEN);
            mCircleProgram.setUniform();
            circle.bindData(mCircleProgram);
            circle.draw();
        }

        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        synchronized (mLock) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(data);
        }

        mSurfaceView.requestRender();

        mCamera.addCallbackBuffer(data);
    }
}
