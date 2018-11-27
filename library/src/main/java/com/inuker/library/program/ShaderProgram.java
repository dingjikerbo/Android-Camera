package com.inuker.library.program;

import android.content.Context;

import com.inuker.library.utils.ResourceUtils;
import com.inuker.library.utils.ShaderHelper;

import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by dingjikerbo on 17/8/16.
 */

public class ShaderProgram {

    protected int mProgram;

    protected int mWidth, mHeight;

    protected final Context mContext;

    protected ShaderProgram(Context context, int vertexId, int fragId) {
        this(context, vertexId, fragId, 0, 0);
    }

    protected ShaderProgram(Context context, int vertexId, int fragId, int width, int height) {
        mContext = context;

        mProgram = ShaderHelper.buildProgram(ResourceUtils.readText(context, vertexId),
                ResourceUtils.readText(context, fragId));

        mWidth = width;
        mHeight = height;
    }

    public void useProgram() {
        glUseProgram(mProgram);
    }

    public void release() {
        glDeleteProgram(mProgram);
        mProgram = -1;
    }
}
