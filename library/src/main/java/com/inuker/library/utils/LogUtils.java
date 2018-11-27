package com.inuker.library.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by liwentian on 17/8/16.
 */

public class LogUtils {

    private static final String TAG = "bush";

    public static void v(String msg) {
        Log.v(TAG, msg);
    }

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void e(Throwable e) {
        String s = getThrowableString(e);
        e(s);
    }

    private static String getThrowableString(Throwable e) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        while (e != null) {
            e.printStackTrace(printWriter);
            e = e.getCause();
        }

        String text = writer.toString();

        printWriter.close();

        return text;
    }
}
