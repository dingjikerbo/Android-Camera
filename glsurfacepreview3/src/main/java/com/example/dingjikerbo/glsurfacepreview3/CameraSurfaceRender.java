package com.example.dingjikerbo.glsurfacepreview3;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import com.inuker.library.LogUtils;
import com.inuker.library.TextureProgram;
import com.inuker.library.WindowSurface;
import com.inuker.library.YUVProgram;

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
import static android.opengl.GLES20.glViewport;

/**
 * Created by liwentian on 17/8/16.
 */

public class CameraSurfaceRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private Camera mCamera;

    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;

    private TextureProgram mTextureProgram;

    private GLSurfaceView mGLSurfaceView;

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

        glViewport(0, 0, width, height);

        int[] textures = new int[1];
        glGenTextures(1, textures, 0);
        mTextureId = textures[0];
        mSurfaceTexture = new SurfaceTexture(mTextureId);

        mTextureProgram = new TextureProgram(mGLSurfaceView.getContext(), width, height);

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
            mSurfaceTexture.setOnFrameAvailableListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(1f, 1f, 1f, 1f);

        mSurfaceTexture.updateTexImage();
        mTextureProgram.draw(mTextureId);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLSurfaceView.requestRender();
    }
}
