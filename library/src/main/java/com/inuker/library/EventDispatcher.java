package com.inuker.library;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liwentian on 17/8/17.
 */

public class EventDispatcher {

    private static final int MSG_OBSERVE = 1;
    private static final int MSG_UNOBSERVE = 2;
    private static final int MSG_DISPATCH = 3;

    private static SparseArray<List<EventListener>> sListeners = new SparseArray<>();

    private static Handler sHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OBSERVE:
                    observeInnser(msg.arg1, (EventListener) msg.obj);
                    break;

                case MSG_UNOBSERVE:
                    unObserveInner(msg.arg1, (EventListener) msg.obj);
                    break;

                case MSG_DISPATCH:
                    dispatchInner(msg.arg1, msg.obj);
                    break;

            }
        }
    };

    public static void observe(int event, EventListener l) {
        sHandler.obtainMessage(MSG_OBSERVE, event, 0, l).sendToTarget();
    }

    private static void observeInnser(int event, EventListener l) {
        if (l == null) {
            return;
        }

        List<EventListener> listeners = sListeners.get(event);
        if (listeners == null) {
            listeners = new LinkedList<>();
            sListeners.put(event, listeners);
        }
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public static void unObserve(int event, EventListener l) {
        sHandler.obtainMessage(MSG_UNOBSERVE, event, 0, l).sendToTarget();
    }

    private static void unObserveInner(int event, EventListener l) {
        if (event <= 0 || l == null) {
            return;
        }

        List<EventListener> listeners = sListeners.get(event);
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    public static void dispatch(final int event, final Object object) {
//        LogUtils.v(String.format("EventDispatcher dispatch: event = %d", event));
        sHandler.obtainMessage(MSG_DISPATCH, event, 0, object).sendToTarget();
    }

    public static void dispatch(int event) {
        dispatch(event, null);
    }

    private static void dispatchInner(final int event, final Object object) {
        LogUtils.v("");
        List<EventListener> listeners = sListeners.get(event);
        if (listeners != null) {
            for (final EventListener l : listeners) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        l.onEvent(event, object);
                    }
                });
            }
        }
    }
}
