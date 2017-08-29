package com.inuker.library;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by liwentian on 17/8/1.
 */

public class AndroidMuxer {

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
        if (mStarted) {
            throw new IllegalStateException();
        }

        synchronized (mMuxer) {
            int track = mMuxer.addTrack(trackFormat);

            if (++mNumTracks == mExpectedNumTracks) {
                mMuxer.start();
                mStarted = true;
            }

            LogUtils.v(String.format("addTrack mNumTracks = %d", mNumTracks));

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

    public boolean release() {
        LogUtils.v("release");
        synchronized (mMuxer) {
            if (++mNumReleases == mNumTracks) {
                LogUtils.v(String.format("Muxer release"));
                mMuxer.stop();
                mMuxer.release();
                return true;
            }
        }
        return false;
    }
}
