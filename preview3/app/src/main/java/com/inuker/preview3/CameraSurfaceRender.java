package com.inuker.preview3;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import com.inuker.preview3.program.TextureProgram;

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
 * Created by liwentian on 17/7/28.
 */

public class CameraSurfaceRender implements GLSurfaceView.Renderer, Camera.PreviewCallback {

    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;

    private TextureProgram mTextureProgram;

    private ByteBuffer mYUVBuffer;

    private GLSurfaceView mSurfaceView;


    public CameraSurfaceRender(GLSurfaceView view) {
        mSurfaceView = view;

        mYUVBuffer = ByteBuffer.allocateDirect(Constants.BUFFER_SIZE)
                .order(ByteOrder.nativeOrder());
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCamera = Camera.open(1);

        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(Constants.WIDTH, Constants.HEIGHT);
        mCamera.setParameters(params);

        Context context = mSurfaceView.getContext();
        mTextureProgram = new TextureProgram(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
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
            byte[] callbackBuffer = new byte[Constants.BUFFER_SIZE];
            mCamera.addCallbackBuffer(callbackBuffer);
        }

        mCamera.startPreview();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(1f, 1f, 1f, 1f);

        mTextureProgram.useProgram();
        synchronized (mYUVBuffer) {
            mTextureProgram.setUniforms(mYUVBuffer.array());
        }
        mTextureProgram.draw();

        mSurfaceTexture.updateTexImage();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (mYUVBuffer) {
            mYUVBuffer.position(0);
            mYUVBuffer.put(data);
        }

        mSurfaceView.requestRender();
        mCamera.addCallbackBuffer(data);
    }
}
