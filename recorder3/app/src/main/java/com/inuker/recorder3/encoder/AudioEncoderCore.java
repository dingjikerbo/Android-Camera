package com.inuker.recorder3.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import com.inuker.recorder3.utils.TaskUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by liwentian on 17/7/30.
 */

public class AudioEncoderCore extends MediaEncoderCore implements Runnable {

    private static final String TAG = "audio_record";

    // AAC Low Overhead Audio Transport Multiplex
    private static final String MIME_TYPE = "audio/mp4a-latm";

    // AAC frame size. Audio encoder input size is a multiple of this
    protected static final int SAMPLES_PER_FRAME = 1024;

    protected static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private int mSampleRate = 44100;

    private int mChannelCount = 1;

    private int mBitRate = 128000;

    private int mMaxInputSize = 16384;

    private AudioRecord mAudioRecord;

    private int mChannelConfig;

    public AudioEncoderCore(AndroidMuxer muxer) {
        super(muxer);

        prepareEncoder();
        prepareRecorder();
    }

    private void prepareEncoder() {
        MediaFormat format = new MediaFormat();

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setString(MediaFormat.KEY_MIME, MIME_TYPE);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, mSampleRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mChannelCount);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mMaxInputSize);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        try {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoder.start();
    }

    private void prepareRecorder() {
        switch (mChannelCount) {
            case 1:
                mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
                break;
            case 2:
                mChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
                break;
            default:
                throw new IllegalArgumentException();
        }

        int minBufferSize = AudioRecord.getMinBufferSize(mSampleRate,
                mChannelConfig, AUDIO_FORMAT);

        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.CAMCORDER, // source
                mSampleRate,            // sample rate, hz
                mChannelConfig,         // channels
                AUDIO_FORMAT,                        // audio format
                minBufferSize * 4);                  // buffer size (bytes)
    }

    @Override
    public void start() {
        if (!recording) {
            recording = true;
            mAudioRecord.startRecording();

            TaskUtils.execute(this);
        }
    }

    @Override
    public void stop() {
        recording = false;
    }

    @Override
    protected boolean isSurfaceInput() {
        return false;
    }

    @Override
    public void run() {
        while (recording) {
            drainEncoder(false);
            drainAudio(false);
        }

        // 这里要先drainAudio，在其后加上end结束符
        drainAudio(true);

        mAudioRecord.stop();
        drainEncoder(true);

        release();
    }

    private void drainAudio(boolean endOfStream) {
//        LogUtils.v(String.format("drainAudio %b", endOfStream));

        ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        int bufferIndex = mEncoder.dequeueInputBuffer(-1); // wait indefinitely
        if (bufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[bufferIndex];
            inputBuffer.clear();

            int len = mAudioRecord.read(inputBuffer, SAMPLES_PER_FRAME * 2); // read blocking
            long ptsUs = System.nanoTime() / 1000;

            if (endOfStream) {
                mEncoder.queueInputBuffer(bufferIndex, 0, len, ptsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                mEncoder.queueInputBuffer(bufferIndex, 0, len, ptsUs, 0);
            }
        }

    }
}
