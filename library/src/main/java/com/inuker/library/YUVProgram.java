package com.inuker.library;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LUMINANCE;
import static android.opengl.GLES20.GL_LUMINANCE_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by liwentian on 17/8/16.
 */

/**
 * 输入Camera的预览NV21数据
 */
public class YUVProgram extends ShaderProgram {

    protected final int mUniformYTextureLocation;
    protected final int mUniformUVTextureLocation;

    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    public static final float TEXTURE_UPSIDE_DOWN[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;

    private int mYTestureId, mUVTextureId;

    private ByteBuffer mYBuffer, mUVBuffer;

    public YUVProgram(Context context, int width, int height) {
        super(context, R.raw.yuv_vertex, R.raw.yuv_fragment, width, height);

        LogUtils.v(String.format("New YUVProgram width = %d, height = %d", width, height));

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

        mYBuffer = ByteBuffer.allocateDirect(width * height)
                .order(ByteOrder.nativeOrder());

        mUVBuffer = ByteBuffer.allocateDirect(width * height / 2)
                .order(ByteOrder.nativeOrder());

        int[] textures = new int[2];
        glGenTextures(2, textures, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textures[0]);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        mYTestureId = textures[0];

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textures[1]);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        mUVTextureId = textures[1];

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);
    }

    public void setUpsideDown() {
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(TEXTURE_UPSIDE_DOWN).position(0);
    }

    public void setUniforms(byte[] data) {
        mYBuffer.position(0);
        mYBuffer.put(data, 0, width * height);

        mUVBuffer.position(0);
        mUVBuffer.put(data, width * height, width * height / 2);

        mYBuffer.position(0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mYTestureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height,
                0, GL_LUMINANCE, GL_UNSIGNED_BYTE, mYBuffer);
        glUniform1i(mUniformYTextureLocation, 0);

        GlUtil.checkGlError("init YTexture");

        mUVBuffer.position(0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, mUVTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, width / 2, height / 2,
                0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, mUVBuffer);
        glUniform1i(mUniformUVTextureLocation, 1);

        GlUtil.checkGlError("init UVTexture");

        mGLCubeBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 0, mGLCubeBuffer);
        glEnableVertexAttribArray(aPositionLocation);

        mGLTextureBuffer.position(0);
        glVertexAttribPointer(aTextureCoordinatesLocation, 2, GL_FLOAT, false, 0, mGLTextureBuffer);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }
}
