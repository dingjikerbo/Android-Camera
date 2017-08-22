package com.inuker.rgbconverter;

import android.content.Context;
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
