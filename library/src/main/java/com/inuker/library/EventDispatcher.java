package com.inuker.library;

import android.util.SparseArray;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dingjikerbo on 17/8/17.
 */

public class EventDispatcher {

    private static SparseArray<List<EventListener>> sListeners = new SparseArray<>();

    public static void observe(int event, EventListener l) {
        if (l == null) {
            return;
        }

        synchronized (sListeners) {
            List<EventListener> listeners = sListeners.get(event);
            if (listeners == null) {
                listeners = new LinkedList<>();
                sListeners.put(event, listeners);
            }
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }
    }

    public static void observe(EventListener l, int... events) {
        synchronized (sListeners) {
            for (int event : events) {
                observe(event, l);
            }
        }
    }

    public static void unObserve(int event, EventListener l) {
        if (l == null) {
            return;
        }

        synchronized (sListeners) {
            List<EventListener> listeners = sListeners.get(event);
            if (listeners != null) {
                listeners.remove(l);
            }
        }
    }

    public static void unObserve(EventListener l, int... events) {
        synchronized (sListeners) {
            for (int event : events) {
                unObserve(event, l);
            }
        }
    }

    private static void dispatch(final int event, final Object object, DispatchCaller caller) {
        synchronized (sListeners) {
            List<EventListener> listeners = sListeners.get(event);
            if (listeners != null) {
                for (final EventListener l : listeners) {
                    caller.onDispatch(event, object, l);
                }
            }
        }
    }

    public static void dispatch(final int event, final Object object) {
        dispatch(event, object, new DispatchCaller() {
            @Override
            public void onDispatch(final int event, final Object object, final EventListener l) {
                if (l != null) {
                    BaseApplication.post(new Runnable() {
                        @Override
                        public void run() {
                            l.onEvent(event, object);
                        }
                    });
                }
            }
        });
    }

    public static void dispatchInstant(final int event, final Object object) {
        dispatch(event, object, new DispatchCaller() {
            @Override
            public void onDispatch(final int event, final Object object, final EventListener l) {
                if (l != null) {
                    l.onEvent(event, object);
                }
            }
        });
    }

    private interface DispatchCaller {
        void onDispatch(int event, Object object, EventListener l);
    }
}
