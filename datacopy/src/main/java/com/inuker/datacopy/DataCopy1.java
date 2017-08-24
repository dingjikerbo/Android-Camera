package com.inuker.datacopy;

/**
 * Created by liwentian on 17/8/24.
 */

public class DataCopy1 extends DataCopy {

    @Override
    void onCopy(byte[] bytes) {
        System.arraycopy(bytes, 0, mBytes, 0, bytes.length);
    }
}
