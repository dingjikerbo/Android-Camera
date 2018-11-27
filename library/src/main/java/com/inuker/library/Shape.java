package com.inuker.library;

import com.inuker.library.program.ShaderProgram;

/**
 * Created by liwentian on 17/6/23.
 */

public abstract class Shape<T extends ShaderProgram> {

    VertexArray mVertexArray;

    float transferX(int x) {
        return 2.0f * x / BaseApplication.getScreenWidth() - 1;
    }

    float transferY(int y) {
        return -2.0f * y / BaseApplication.getScreenHeight() + 1;
    }

    float transferL(int l) {
        return 2f * l / BaseApplication.getScreenHeight();
    }

    public abstract void bindData(T program);

    public abstract void draw();
}
