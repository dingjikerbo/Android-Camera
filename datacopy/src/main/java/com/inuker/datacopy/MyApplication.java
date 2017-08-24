package com.inuker.datacopy;

import com.inuker.library.BaseApplication;
import com.inuker.library.LogUtils;

/**
 * Created by liwentian on 17/8/24.
 */

public class MyApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.v(String.format("start at %d", System.currentTimeMillis()));

        NativeCopy.init();
    }
}
