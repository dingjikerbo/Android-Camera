package com.hobot.smarthome.testcamera;

import android.content.Context;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.Matrix.orthoM;

/**
 * Created by liwentian on 17/6/22.
 */

public class CircleProgram extends ShapeProgram {

    private final int uMatrixLocation;

    private final float[] projectionMatrix = new float[16];

    protected CircleProgram(Context context) {
        super(context, R.raw.circle_vertex, R.raw.circle_fragment);

        uMatrixLocation = glGetUniformLocation(program, "u_Matrix");
        float ratio = 1.0f * MyApplication.getScreenWidth() / MyApplication.getScreenHeight();
        orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f);
    }

    public void setUniform() {
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
    }
}
