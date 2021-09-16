package com.jieluote.josp.handler;

import android.os.SystemClock;
import android.util.Log;

public class Handlers {
    private Loopers mLoopers;
    private MessagesQueues mMessagesQueues;
    private final Callback mCallback;

    public interface Callback {
        public boolean handleMessage(Messages msg);
    }

    public Handlers() {
        this(null, null);
    }

    public Handlers(Callback callback) {
        this(null, callback);
    }

    public Handlers(Loopers loopers) {
        this(loopers, null);
    }

    public Loopers getLoopers() {
        return mLoopers;
    }

    public Handlers(Loopers loopers, Callback callback) {
        mCallback = callback;
        if (loopers == null) {
            mLoopers = Loopers.MyLooper();
        } else {
            mLoopers = loopers;
        }
        if (mLoopers == null) {
            throw new RuntimeException("Can't create handler inside thread that has not called Loopers.prepare()");
        }
        mMessagesQueues = mLoopers.mQueue;
    }

    public void sendMessage(Messages msg) {
        sendMessageDelayed(msg, 0);
    }

    public void sendMessageDelayed(Messages msg, long delayMillis) {
        Log.d("HandlersTest", "sendMessage ,mQueue:" + mMessagesQueues);
        if (mMessagesQueues == null) {
            RuntimeException e = new RuntimeException(
                    this + " sendMessage() called with no mQueue");
            Log.e("HandlersTest", e.getMessage(), e);
        }
        msg.target = this;
        if(delayMillis < 0){
            delayMillis = 0;
        }
        mMessagesQueues.enqueueMessage(msg, SystemClock.uptimeMillis() + delayMillis);
    }

    public void dispatchMessages(Messages msg) {
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }

    private static void handleCallback(Messages msg) {
        msg.callback.run();
    }

    public void handleMessage(Messages msg) {

    }

}
