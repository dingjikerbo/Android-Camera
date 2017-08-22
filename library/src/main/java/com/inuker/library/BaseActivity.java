package com.inuker.library;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

/**
 * Created by liwentian on 17/8/16.
 */

public class BaseActivity extends Activity implements Handler.Callback {

    protected Handler mHandler = new Handler(Looper.getMainLooper(), this);

    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    public void postDelayed(Runnable runnable, long delayInMillis) {
        mHandler.postDelayed(runnable, delayInMillis);
    }
}
