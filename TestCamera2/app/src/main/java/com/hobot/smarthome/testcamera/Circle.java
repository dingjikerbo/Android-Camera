package com.hobot.smarthome.testcamera;

import android.graphics.Color;

import static android.opengl.GLES20.GL_LINE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glLineWidth;

/**
 * Created by liwentian on 17/6/23.
 */

public class Circle extends Shape<CircleProgram> {

    private static final int POINT_SIZE = 50;

    private static final int POSITION_COUNT = 2;
    private static final int COLOR_COUNT = 4;
    private static final int STRIDE = (POSITION_COUNT + COLOR_COUNT) * 4;

    public Circle(int x, int y, int radius, int color) {
        float xs = transferX(x), ys = transferY(y), rs = transferL(radius);

        float r = Color.red(color) / 255f;
        float g = Color.green(color) / 255f;
        float b = Color.blue(color) / 255f;
        float a = Color.alpha(color) / 255f;

        float[] data = new float[(POINT_SIZE + 1) * STRIDE];

        float angle = (float) (2 * Math.PI / POINT_SIZE);

        for (int i = 0, offset = 0; i <= POINT_SIZE; i++) {
            float angNow = angle * i;
            data[offset++] = (float) (xs + rs * Math.cos(angNow));
            data[offset++] = (float) (ys + rs * Math.sin(angNow));
            data[offset++] = r;
            data[offset++] = g;
            data[offset++] = b;
            data[offset++] = a;
        }

        mVertexArray = new VertexArray(data);
    }

    @Override
    public void bindData(CircleProgram program) {
        mVertexArray.setVertexAttribPointer(0, program.getPositionLocation(),
                POSITION_COUNT, STRIDE);

        mVertexArray.setVertexAttribPointer(POSITION_COUNT, program.getColorLocation(),
                COLOR_COUNT, STRIDE);
    }

    @Override
    public void draw() {
        glLineWidth(10f);
        glDrawArrays(GL_LINE_STRIP, 0, POINT_SIZE + 1);
    }
}
