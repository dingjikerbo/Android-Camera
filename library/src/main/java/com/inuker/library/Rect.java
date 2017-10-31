package com.inuker.library;

import android.graphics.Color;


import static android.opengl.GLES20.GL_LINE_STRIP;
import static android.opengl.GLES20.glDrawArrays;

/**
 * Created by liwentian on 17/6/23.
 */

public class Rect extends Shape<RectProgram> {

    private static final int POSITION_COUNT = 2;
    private static final int COLOR_COUNT = 4;
    private static final int STRIDE = (POSITION_COUNT + COLOR_COUNT) * 4;
    private static final int POINTS_COUNT = 5;

    public Rect(int x1, int y1, int x2, int y2, int color) {
        float x1s = transferX(x1);
        float y1s = transferY(y1);
        float x2s = transferX(x2);
        float y2s = transferY(y2);

        float r = Color.red(color) / 255f;
        float g = Color.green(color) / 255f;
        float b = Color.blue(color) / 255f;
        float a = Color.alpha(color) / 255f;

        float[] data = new float[] {
                x1s, y1s, r, g, b, a,
                x1s, y2s, r, g, b, a,
                x2s, y2s, r, g, b, a,
                x2s, y1s, r, g, b, a,
                x1s, y1s, r, g, b, a,
        };

        mVertexArray = new VertexArray(data);
    }

    @Override
    public void bindData(RectProgram program) {
        mVertexArray.setVertexAttribPointer(0, program.getPositionLocation(),
                POSITION_COUNT, STRIDE);

        mVertexArray.setVertexAttribPointer(POSITION_COUNT, program.getColorLocation(),
                COLOR_COUNT, STRIDE);
    }

    @Override
    public void draw() {
        glDrawArrays(GL_LINE_STRIP, 0, POINTS_COUNT);
    }
}
