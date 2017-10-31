package com.inuker.library;

import android.content.Context;

import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by liwentian on 2017/10/31.
 */

public class RectProgram extends ShapeProgram {

    public RectProgram(Context context) {
        super(context, R.raw.rect_vertex, R.raw.rect_fragment);
    }

    public void setUniform() {
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);
    }
}
