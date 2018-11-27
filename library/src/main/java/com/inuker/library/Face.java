package com.inuker.library;

import static android.opengl.GLES20.glLineWidth;

/**
 * Created by dingjikerbo on 2017/10/31.
 */

public class Face extends Rect {

    public Face(int x1, int y1, int x2, int y2, int color) {
        super(x1, y1, x2, y2, color);
    }

    @Override
    public void draw() {
        glLineWidth(5f);
        super.draw();
    }
}
