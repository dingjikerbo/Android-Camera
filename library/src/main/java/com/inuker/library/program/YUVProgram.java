package com.inuker.library.program;

import android.content.Context;
import android.content.res.Configuration;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.inuker.library.R;

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

    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    // Attribute locations
    private final int mPositionLocation;
    private final int mTextureCoordinatesLocation;

    private final int mMVPMatrixLocation;

    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;

    private int mYTestureId, mUVTextureId;

    private ByteBuffer mYBuffer, mUVBuffer;

    private float[] mMatrix = new float[16];

    public YUVProgram(Context context, int width, int height) {
        super(context, R.raw.yuv_vertex, R.raw.yuv_fragment);

        mWidth = Math.max(width, height);
        mHeight = Math.min(width, height);

        mUniformYTextureLocation = glGetUniformLocation(mProgram, "y_texture");
        mUniformUVTextureLocation = glGetUniformLocation(mProgram, "uv_texture");
        mMVPMatrixLocation = glGetUniformLocation(mProgram, "uMVPMatrix");

        mPositionLocation = glGetAttribLocation(mProgram, "a_Position");
        mTextureCoordinatesLocation = glGetAttribLocation(mProgram, "a_TextureCoordinates");

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

    public void draw(byte[] data) {
        useProgram();

        mYBuffer.position(0);
        mYBuffer.put(data, 0, mWidth * mHeight);

        mUVBuffer.position(0);
        mUVBuffer.put(data, mWidth * mHeight, mWidth * mHeight / 2);

        mYBuffer.position(0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mYTestureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, mWidth, mHeight,
                0, GL_LUMINANCE, GL_UNSIGNED_BYTE, mYBuffer);
        glUniform1i(mUniformYTextureLocation, 0);

        mUVBuffer.position(0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, mUVTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, mWidth / 2, mHeight / 2,
                0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, mUVBuffer);
        glUniform1i(mUniformUVTextureLocation, 1);

        Matrix.setIdentityM(mMatrix, 0);
        int orientation = mContext.getResources().getConfiguration().orientation;
        int degrees = orientation == Configuration.ORIENTATION_LANDSCAPE ? 0 : -90;
        Matrix.rotateM(mMatrix, 0, degrees, 0.0f, 0.0f, 1.0f);

        GLES20.glUniformMatrix4fv(mMVPMatrixLocation, 1, false, mMatrix, 0);

        mGLCubeBuffer.position(0);
        glVertexAttribPointer(mPositionLocation, 2, GL_FLOAT, false, 0, mGLCubeBuffer);
        glEnableVertexAttribArray(mPositionLocation);

        mGLTextureBuffer.position(0);
        glVertexAttribPointer(mTextureCoordinatesLocation, 2, GL_FLOAT, false, 0, mGLTextureBuffer);
        glEnableVertexAttribArray(mTextureCoordinatesLocation);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }
}
