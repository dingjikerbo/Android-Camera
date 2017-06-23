package com.hobot.smarthome.testcamera;

import android.content.Context;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.Matrix.orthoM;

/**
 * Created by liwentian on 17/6/23.
 */

public class ShapeProgram extends ShaderProgram {

    private final int aPositionLocation;

    private final int aColorLocation;

    protected ShapeProgram(Context context, int vertex, int fragment) {
        super(context, vertex, fragment);

        useProgram();
        aPositionLocation = glGetAttribLocation(program, "a_Position");
        aColorLocation = glGetAttribLocation(program, "a_Color");
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getColorLocation() {
        return aColorLocation;
    }
}
