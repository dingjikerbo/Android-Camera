package com.inuker.library.program;

import android.content.Context;
import android.content.res.Configuration;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.inuker.library.R;
import com.inuker.library.utils.GlUtil;

import java.nio.FloatBuffer;

public class OESProgram extends ShaderProgram {

    private int muTexMatrixLoc;
    private int muMVPMatrixLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;

    private int mTextureTarget;

    private float[] mMVPMatrix = new float[16];

    private static final float RECTANGLE_COORDS[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
    };
    private static final float RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };
    private static final FloatBuffer RECTANGLE_BUF =
            GlUtil.createFloatBuffer(RECTANGLE_COORDS);
    private static final FloatBuffer RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(RECTANGLE_TEX_COORDS);

    public OESProgram(Context context, int width, int height) {
        super(context, R.raw.oes_vertex, R.raw.oes_fragment);

        mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

        maPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");
        GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
    }

    public void draw(int textureId, float[] texMatrix) {
        GlUtil.checkGlError("draw start");

        // Select the program.
        GLES20.glUseProgram(mProgram);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mTextureTarget, textureId);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        Matrix.setIdentityM(mMVPMatrix, 0);
        int orientation = mContext.getResources().getConfiguration().orientation;
        int degrees = orientation == Configuration.ORIENTATION_PORTRAIT ? 90 : 180;
        Matrix.rotateM(mMVPMatrix, 0, degrees, 0.0f, 0.0f, 1.0f);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, 2,
                GLES20.GL_FLOAT, false, 2 * 4, RECTANGLE_BUF);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, 8, RECTANGLE_TEX_BUF);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);
    }
}
