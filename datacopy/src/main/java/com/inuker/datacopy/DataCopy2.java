package com.inuker.datacopy;

import android.provider.ContactsContract;

/**
 * Created by liwentian on 17/8/24.
 */

public class DataCopy2 extends DataCopy {

    @Override
    void onCopy(byte[] bytes) {
        NativeCopy.write(bytes);
    }
}
