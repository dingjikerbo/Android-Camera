package com.inuker.datacopy;

import com.inuker.library.LogUtils;

/**
 * Created by liwentian on 17/8/24.
 */

public class DataCopy3 extends DataCopy {

    @Override
    void onCopy(byte[] bytes) {
        NativeCopy.read(bytes);
    }
}
