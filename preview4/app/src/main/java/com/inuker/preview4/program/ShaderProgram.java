package com.inuker.preview4.program;

import android.content.Context;

import com.inuker.preview4.ResourceUtils;
import com.inuker.preview4.ShaderHelper;

import static android.opengl.GLES20.glUseProgram;

/**
 * Created by liwentian on 17/6/22.
 */

public class ShaderProgram {

    protected final int program;

    protected ShaderProgram(Context context, int vertexId, int fragId) {
        program = ShaderHelper.buildProgram(ResourceUtils.readText(context, vertexId),
                ResourceUtils.readText(context, fragId));
    }

    public void useProgram() {
        glUseProgram(program);
    }
}
