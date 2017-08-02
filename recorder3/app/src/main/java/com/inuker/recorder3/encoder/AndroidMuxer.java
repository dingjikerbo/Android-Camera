package com.inuker.recorder3.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.inuker.recorder3.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by liwentian on 17/8/1.
 */

public class AndroidMuxer {

    protected static final String TAG = Constants.TAG;

    private final int mExpectedNumTracks = 2;

    private MediaMuxer mMuxer;

    private volatile boolean mStarted;

    private volatile int mNumTracks;
    private volatile int mNumReleases;

    public AndroidMuxer(String outputPath) {
        try {
            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int addTrack(MediaFormat trackFormat) {
        Log.v(TAG, String.format("%s.addTracker", getClass().getSimpleName()));

        if (mStarted) {
            throw new IllegalStateException();
        }

        synchronized (mMuxer) {
            int track = mMuxer.addTrack(trackFormat);

            if (++mNumTracks == mExpectedNumTracks) {
                Log.v(TAG, String.format("%s start", getClass().getSimpleName()));
                mMuxer.start();
                mStarted = true;
            }

            return track;
        }
    }

    public boolean isStarted() {
        return mStarted;
    }

    public void writeSampleData(int trackIndex, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        synchronized (mMuxer) {
            mMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
        }
    }

    public void release() {
        synchronized (mMuxer) {
            if (++mNumReleases == mNumTracks) {
                if (mStarted) {
                    mMuxer.stop();
                    mMuxer.release();
                }
            }
        }
    }
}
