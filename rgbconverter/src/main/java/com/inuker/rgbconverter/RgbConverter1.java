package com.inuker.rgbconverter;

import android.content.Context;

/**
 * Created by liwentian on 17/8/21.
 */

public class RgbConverter1 extends RgbConverter {

    public RgbConverter1(Context context) {
        super(context);
    }

    @Override
    void onStart() {

    }

    @Override
    void onDrawFrame() {
        readPixels();
        pixelsToBitmap();
    }

    @Override
    void onDestroy() {

    }
}
