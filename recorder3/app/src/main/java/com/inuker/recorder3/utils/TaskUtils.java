package com.inuker.recorder3.utils;

import android.os.AsyncTask;

import java.util.concurrent.Executor;

/**
 * Created by liwentian on 17/8/2.
 */

public class TaskUtils {

    public static void execute(final Runnable runnable) {
        execute(AsyncTask.THREAD_POOL_EXECUTOR, runnable);
    }

    public static void execute(final Executor executor, final Runnable runnable) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                runnable.run();
                return null;
            }
        }.executeOnExecutor(executor);
    }
}
