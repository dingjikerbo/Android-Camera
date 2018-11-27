package com.inuker.rgbconverter;

/**
 * Created by dingjikerbo on 17/8/22.
 */

public interface IRgbConverter {

    void start();

    void destroy();

    void frameDrawed();

    void frameAvailable(byte[] bytes);
}
