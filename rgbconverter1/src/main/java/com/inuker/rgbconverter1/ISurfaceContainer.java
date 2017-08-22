package com.inuker.rgbconverter1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by liwentian on 17/8/22.
 */

public interface ISurfaceContainer {

    Context getContext();

    ViewGroup getFullSurfaceContainer();

    ViewGroup getMiniSurfaceContainer();

    RgbConverter getRgbConverter();
}
