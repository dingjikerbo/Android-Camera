package com.inuker.library;

import android.content.Context;

/**
 * Created by liwentian on 2017/10/31.
 */

public class MovieEncoder2 extends BaseMovieEncoder {

    private int mTexture;

    private TextureProgram mProgram;

    public MovieEncoder2(Context context, int width, int height) {
        super(context, width, height);
    }

    @Override
    public void onPrepareEncoder() {
        mProgram = new TextureProgram(mContext, mWidth, mHeight);
    }

    @Override
    public void onFrameAvailable(Object o, long timestamp) {
        mTexture = (int) o;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE,
                (int) (timestamp >> 32), (int) timestamp));
    }

    @Override
    public void onFrameAvailable() {
        mProgram.draw(mTexture);
    }
}
