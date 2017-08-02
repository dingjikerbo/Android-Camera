package com.inuker.recorder3.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liwentian on 17/8/2.
 */

public class CameraHelper {

    public static File getOutputVideoFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        File mediaStorageDir = Environment.getExternalStoragePublicDirectory("video");
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            throw new IllegalStateException();
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4");
    }
}
