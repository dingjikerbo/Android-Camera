package com.inuker.datacopy;

import com.inuker.library.BaseApplication;
import com.inuker.library.EventDispatcher;
import com.inuker.library.EventListener;
import com.inuker.library.RuntimeCounter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.inuker.datacopy.Events.EVENTS_FRAME_AVAILABLE;
import static com.inuker.datacopy.Events.EVENTS_TIME_UPDATE;

/**
 * Created by liwentian on 17/8/24.
 */

public abstract class DataCopy implements EventListener {

    protected RuntimeCounter mCounter;

    protected byte[] mBytes;

    public DataCopy() {
        mCounter = new RuntimeCounter();
        mBytes = new byte[BaseApplication.getScreenWidth() * BaseApplication.getScreenHeight() * 4];
    }

    public void start() {
        EventDispatcher.observe(EVENTS_FRAME_AVAILABLE, this);
    }

    public void stop() {
        EventDispatcher.unObserve(EVENTS_FRAME_AVAILABLE, this);
    }

    @Override
    public void onEvent(int event, Object object) {
        switch (event) {
            case EVENTS_FRAME_AVAILABLE:
                mCounter.start();
                onCopy((byte[]) object);
                mCounter.end();
                EventDispatcher.dispatch(EVENTS_TIME_UPDATE, mCounter.getAvg());
                break;
        }
    }

    public static DataCopy load(Class<? extends DataCopy> clazz) {
        Constructor constructor = null;
        try {
            constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            return (DataCopy) constructor.newInstance();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    abstract void onCopy(byte[] bytes);
}
