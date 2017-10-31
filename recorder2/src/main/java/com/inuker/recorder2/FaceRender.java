package com.inuker.recorder2;

import android.content.Context;
import android.graphics.Color;

import com.inuker.library.Face;
import com.inuker.library.LogUtils;
import com.inuker.library.RectProgram;

/**
 * Created by liwentian on 2017/10/31.
 */

public class FaceRender {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;
    private static final int XSTEP = 10;
    private static final int YSTEP = 10;

    private RectProgram mProgram;

    private int mX, mY;

    private boolean mXDirect = true, mYDirect = true;

    public FaceRender(Context context) {
        mProgram = new RectProgram(context);
    }

    public void draw() {
        mProgram.useProgram();
        mProgram.setUniform();

        Face face = new Face(mX, mY, mX + WIDTH, mY + HEIGHT, Color.RED);

        face.bindData(mProgram);
        face.draw();

        updateNext();
    }

    private void updateNext() {
        if (mXDirect) {
            mX += XSTEP;
            if (mX + WIDTH > MyApplication.getScreenWidth()) {
                mX = MyApplication.getScreenWidth() - WIDTH;
                mXDirect = false;
            }
        } else {
            mX -= XSTEP;
            if (mX < 0) {
                mX = 0;
                mXDirect = true;
            }
        }
        if (mYDirect) {
            mY += YSTEP;
            if (mY + HEIGHT > MyApplication.getScreenHeight()) {
                mY = MyApplication.getScreenHeight() - HEIGHT;
                mYDirect = false;
            }
        } else {
            mY -= YSTEP;
            if (mY < 0) {
                mY = 0;
                mYDirect = true;
            }
        }
    }
}
