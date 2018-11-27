package com.inuker.library;

/**
 * Created by dingjikerbo on 17/8/22.
 */

public class RuntimeCounter {

    private int mCount;

    public long mSum;

    private volatile long mStart;

    public void add(long time) {
        mCount++;
        mSum += time;
    }

    public int getAvg() {
        return mCount > 0 ? (int) (mSum / mCount) : 0;
    }

    public void clear() {
        mCount = 0;
        mSum = 0;
    }

    public void start() {
        mStart = System.currentTimeMillis();
    }

    public void end() {
        long now = System.currentTimeMillis();
        add(now - mStart);
        mStart = now;
    }
}
