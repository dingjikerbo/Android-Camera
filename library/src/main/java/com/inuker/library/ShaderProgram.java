package com.inuker.library;

import android.content.Context;

import static android.opengl.GLES20.glUseProgram;

/**
 * Created by liwentian on 17/8/16.
 */

public class ShaderProgram {

    protected final int program;

    protected final int width, height;

    protected ShaderProgram(Context context, int vertexId, int fragId, int width, int height) {
        program = ShaderHelper.buildProgram(ResourceUtils.readText(context, vertexId),
                ResourceUtils.readText(context, fragId));

        this.width = width;
        this.height = height;
    }

    public void useProgram() {
        glUseProgram(program);
    }
}
