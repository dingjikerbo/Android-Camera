package com.inuker.library;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by liwentian on 17/8/16.
 */

public class BaseActivity extends Activity implements Handler.Callback {

    protected Handler mHandler = new Handler(Looper.getMainLooper(), this);

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
