package com.inuker.library.program;

import android.content.Context;

import com.inuker.library.utils.GlUtil;
import com.inuker.library.R;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by liwentian on 17/8/16.
 */

public class TextureProgram extends ShaderProgram {

    private static final float FULL_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
    };

    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };
    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);

    protected final int mUniformTextureLocation;

    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    public TextureProgram(Context context, int vertexShader, int fragmentShader, int width, int height) {
        super(context, vertexShader, fragmentShader, width, height);

        mUniformTextureLocation = glGetUniformLocation(mProgram, "s_texture");

        aPositionLocation = glGetAttribLocation(mProgram, "a_Position");
        aTextureCoordinatesLocation = glGetAttribLocation(mProgram, "a_TextureCoordinates");
    }


    public TextureProgram(Context context, int width, int height) {
        this(context, R.raw.tex_vertex, R.raw.tex_fragment, width, height);
    }

    public void draw(int texture) {
        useProgram();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);

        glEnableVertexAttribArray(aPositionLocation);
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 8, FULL_RECTANGLE_BUF);

        glEnableVertexAttribArray(aTextureCoordinatesLocation);
        glVertexAttribPointer(aTextureCoordinatesLocation, 2, GL_FLOAT, false, 8, FULL_RECTANGLE_TEX_BUF);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }
}
