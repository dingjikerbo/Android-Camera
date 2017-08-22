package com.inuker.rgbconverter1;

/**
 * Created by liwentian on 17/8/22.
 */

public interface IRgbConverter {

    void start();

    void destroy();

    void frameAvailable(byte[] bytes);
}
