package com.inuker.library;

import android.content.Context;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.Matrix.setIdentityM;

/**
 * Created by liwentian on 2017/10/31.
 */

public class ShapeProgram extends ShaderProgram {

    private final int aPositionLocation;

    private final int aColorLocation;

    protected final int uMatrixLocation;

    protected float[] mMatrix = new float[16];

    protected ShapeProgram(Context context, int vertex, int fragment) {
        super(context, vertex, fragment);

        useProgram();
        aPositionLocation = glGetAttribLocation(program, "a_Position");
        aColorLocation = glGetAttribLocation(program, "a_Color");
        uMatrixLocation = glGetUniformLocation(program, "u_Matrix");

        setIdentityM(mMatrix, 0);
//        scaleM(mMatrix, 0, -1, 1, 1);
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getColorLocation() {
        return aColorLocation;
    }
}
