package com.inuker.library;

import android.content.Context;

import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by liwentian on 17/8/16.
 */

public class ShaderProgram {

    protected int program;

    protected final int width, height;

    protected Context context;

    protected ShaderProgram(Context context, int vertexId, int fragId) {
        this(context, vertexId, fragId, 0, 0);
    }

    protected ShaderProgram(Context context, int vertexId, int fragId, int width, int height) {
        this.context = context;

        program = ShaderHelper.buildProgram(ResourceUtils.readText(context, vertexId),
                ResourceUtils.readText(context, fragId));

        this.width = Math.max(width, height);
        this.height = Math.min(width, height);
    }

    public void useProgram() {
        glUseProgram(program);
    }

    public void release() {
        glDeleteProgram(program);
        program = -1;
    }
}
