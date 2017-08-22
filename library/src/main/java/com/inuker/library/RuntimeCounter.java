package com.inuker.library;

/**
 * Created by liwentian on 17/8/22.
 */

public class RuntimeCounter {

    private int mCount;

    public long mSum;

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
}
