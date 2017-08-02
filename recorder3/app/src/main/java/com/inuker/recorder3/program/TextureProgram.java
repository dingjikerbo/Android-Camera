package com.inuker.recorder3.program;

import android.content.Context;
import android.opengl.GLES20;

import com.inuker.recorder3.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;

/**
 * Created by liwentian on 17/6/22.
 */

public class TextureProgram extends ShaderProgram {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int SIZE = WIDTH * HEIGHT;

    protected final int mUniformYTextureLocation;
    protected final int mUniformUVTextureLocation;

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

    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;

    private int mYTestureId, mUVTextureId;

    private ByteBuffer mYBuffer, mUVBuffer;

    public TextureProgram(Context context) {
        super(context, R.raw.texture_vertex, R.raw.texture_fragment);

        mUniformYTextureLocation = glGetUniformLocation(program, "y_texture");
        mUniformUVTextureLocation = glGetUniformLocation(program, "uv_texture");

        aPositionLocation = glGetAttribLocation(program, "a_Position");
        aTextureCoordinatesLocation = glGetAttribLocation(program, "a_TextureCoordinates");

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

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);
    }

    public void setUniforms(byte[] data) {
        mYBuffer.position(0);
        mYBuffer.put(data, 0, SIZE);

        mUVBuffer.position(0);
        mUVBuffer.put(data, SIZE, SIZE / 2);

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

        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(aTextureCoordinatesLocation, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoordinatesLocation);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void release() {

    }
}
