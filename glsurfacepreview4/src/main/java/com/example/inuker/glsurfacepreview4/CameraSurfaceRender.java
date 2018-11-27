package com.example.inuker.glsurfacepreview4;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import com.inuker.library.program.OESProgram;

import java.io.IOException;
import java.lang.invoke.MutableCallSite;

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

    // 顺时针旋转
    private static final int ROTATE_0 = 0;
    private static final int ROTATE_90 = 1;
    private static final int ROTATE_180 = 2;
    private static final int ROTATE_270 = 3;

    private Camera mCamera;

    private int mSurfaceTextureId;
    private SurfaceTexture mSurfaceTexture;
    private float[] mTransformMatrix = new float[16];

    private GLSurfaceView mGLSurfaceView;

    private OESProgram mProgram;

    public CameraSurfaceRender(GLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.v("bush", String.format("onSurfaceCreated"));
        mCamera = Camera.open(1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.v("bush", String.format("onSurfaceChanged"));

        glViewport(0, 0, width, height);

        int[] textures = new int[1];
        glGenTextures(1, textures, 0);
        mSurfaceTextureId = textures[0];
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mProgram = new OESProgram(mGLSurfaceView.getContext(), width, height);

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.v("bush", "onDrawFrame");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(1f, 1f, 1f, 1f);

        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTransformMatrix);

        StringBuilder sb = new StringBuilder();
        for (float f : mTransformMatrix) {
            sb.append(String.format("%.2f, ", f));
        }
        Log.v("bush", String.format("%s", sb.toString()));

        mProgram.draw(mSurfaceTextureId, mTransformMatrix);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.v("bush", "onFrameAvailable");
        mGLSurfaceView.requestRender();
    }
}
