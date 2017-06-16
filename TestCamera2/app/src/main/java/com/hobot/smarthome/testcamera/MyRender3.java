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
 * 用OpenGL转NV21到RGB
 */
public class MyRender3 implements GLSurfaceView.Renderer, Camera.PreviewCallback {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int SIZE = WIDTH * HEIGHT;

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
            "varying vec2 v_texCoord;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    v_texCoord = inputTextureCoordinate.xy;\n" +
            "}";

    public static final String NO_FILTER_FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D y_texture;\n" +
                    "uniform sampler2D uv_texture;\n" +
                    "void main() {\n" +
                    "float r, g, b, y, u, v;\n" +
                    "y = texture2D(y_texture, v_texCoord).r;\n" +
                    "u = texture2D(uv_texture, v_texCoord).a - 0.5;\n" +
                    "v = texture2D(uv_texture, v_texCoord).r - 0.5;\n" +
                    "r = y + 1.13983 * v;\n" +
                    "g = y - 0.39465 * u - 0.58060 * v;\n" +
                    "b = y + 2.03211 * u;\n" +
                    "gl_FragColor = vec4(r, g, b, 1.0);\n" +
                    "}";

    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;

    protected int mGLProgId;
    protected int mGLAttribPosition;
    protected int mUniformYTextureLocation;
    protected int mUniformUVTextureLocation;
    protected int mGLAttribTextureCoordinate;

    private ByteBuffer mYBuffer, mUVBuffer;

    private final Object mLock = new Object();

    private int mYTestureId, mUVTextureId;

    private volatile boolean mSurfaceCreated;

    public MyRender3(GLSurfaceView view) {
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

        mYBuffer = ByteBuffer.allocateDirect(WIDTH * HEIGHT)
                .order(ByteOrder.nativeOrder());

        mUVBuffer = ByteBuffer.allocateDirect(WIDTH * HEIGHT / 2)
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

        mGLProgId = OpenGlUtils.loadProgram(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
        LogUtils.v(String.format("program = %d", mGLProgId));

        mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "position");
        LogUtils.v(String.format("mGLAttribPosition = %d", mGLAttribPosition));

        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate");
        LogUtils.v(String.format("mGLAttribTextureCoordinate = %d", mGLAttribTextureCoordinate));

        mUniformYTextureLocation = GLES20.glGetUniformLocation(mGLProgId, "y_texture");
        LogUtils.v(String.format("mUniformYTextureLocation = %d", mUniformYTextureLocation));

        mUniformUVTextureLocation = GLES20.glGetUniformLocation(mGLProgId, "uv_texture");
        LogUtils.v(String.format("mUniformUVTextureLocation = %d", mUniformUVTextureLocation));

        int[] textures = new int[2];
        GLES20.glGenTextures(2, textures, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        mYTestureId = textures[0];

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        mUVTextureId = textures[1];

        LogUtils.v(String.format("yTexture = %d, uvTexture = %d", mYTestureId, mUVTextureId));
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

        if (!mSurfaceCreated) {
            return;
        }

        synchronized (mLock) {
            mYBuffer.position(0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYTestureId);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, WIDTH, HEIGHT,
                    0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mYBuffer);
            GLES20.glUniform1i(mUniformYTextureLocation, 0);

            mUVBuffer.position(0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUVTextureId);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, WIDTH / 2, HEIGHT / 2,
                    0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, mUVBuffer);

            GLES20.glUniform1i(mUniformUVTextureLocation, 1);
        }

        GLES20.glUseProgram(mGLProgId);

        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        Log.v("bush", "onPreviewFrame");

        synchronized (mLock) {
            mYBuffer.position(0);
            mYBuffer.put(data, 0, SIZE);

            mUVBuffer.position(0);
            mUVBuffer.put(data, SIZE, SIZE / 2);
        }

        mCamera.addCallbackBuffer(data);
    }
}
