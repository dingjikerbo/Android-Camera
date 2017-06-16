package com.hobot.smarthome.testcamera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by liwentian on 17/6/15.
 */

/**
 * 将NV21中的Y分量传给OpenGL
 */
public class MyRender2 implements GLSurfaceView.Renderer, Camera.PreviewCallback {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private GLSurfaceView mSurfaceView;

    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;

    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    public static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    public static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;

    protected int mGLProgId;
    protected int mGLAttribPosition;
    protected int mGLUniformTexture;
    protected int mGLAttribTextureCoordinate;

    private IntBuffer mGLRgbBuffer;

    private int mGLTextureId = -1;

    public MyRender2(GLSurfaceView view) {
        mSurfaceView = view;

        mCamera = Camera.open(1);

        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(WIDTH, HEIGHT);
        params.setPreviewFpsRange(30000, 30000);
        mCamera.setParameters(params);

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mGLRgbBuffer = IntBuffer.allocate(WIDTH * HEIGHT);
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
        GLES20.glClearColor(0, 0, 0, 1);

        mGLProgId = OpenGlUtils.loadProgram(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
        LogUtils.v(String.format("program = %d", mGLProgId));

        mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "position");
        LogUtils.v(String.format("mGLAttribPosition = %d", mGLAttribPosition));

        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgId, "inputImageTexture");
        LogUtils.v(String.format("mGLUniformTexture = %d", mGLUniformTexture));

        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate");
        LogUtils.v(String.format("mGLAttribTextureCoordinate = %d", mGLAttribTextureCoordinate));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(mGLProgId);

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);

        startPreview();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.v("bush", "onDrawFrame");

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(1f, 1f, 1f, 1f);

        synchronized (mGLRgbBuffer) {
            mGLRgbBuffer.position(0);
            mGLTextureId = OpenGlUtils.loadTexture(mGLRgbBuffer, mCamera.getParameters().getPreviewSize(), mGLTextureId);
        }

        GLES20.glUseProgram(mGLProgId);

        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        if (mGLTextureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTextureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        Log.v("bush", "onPreviewFrame");

        synchronized (mGLRgbBuffer) {
            mGLRgbBuffer.position(0);
            for (int i = 0; i < WIDTH * HEIGHT; i++) {
                mGLRgbBuffer.put(data[i] & 0xff);
            }
        }

        mCamera.addCallbackBuffer(data);
    }
}
