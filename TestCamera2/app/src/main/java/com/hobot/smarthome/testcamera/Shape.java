package com.hobot.smarthome.testcamera;

import android.graphics.Shader;

/**
 * Created by liwentian on 17/6/23.
 */

public abstract class Shape<T extends ShaderProgram> {

    VertexArray mVertexArray;

    float transferX(int x) {
        return 2.0f * x / MyApplication.getScreenWidth() - 1;
    }

    float transferY(int y) {
        return -2.0f * y / MyApplication.getScreenHeight() + 1;
    }

    float transferL(int l) {
        return 2f * l / MyApplication.getScreenHeight();
    }

    public abstract void bindData(T program);

    public abstract void draw();
}
