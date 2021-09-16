package com.jieluote.josp.handler;

import android.util.Log;
import android.util.Printer;

import androidx.annotation.Nullable;

public class Loopers {
    static final ThreadLocal<Loopers> sThreadLocal = new ThreadLocal<Loopers>();
    public MessagesQueues mQueue;
    public Runnable mThread;
    private static Loopers sMainLooper;
    private static boolean isQuit;
    private Printer mLogging;   //用于记录消息分发,可替换为自定义Printer,实现对应用性能的监控

    private Loopers() {
        mQueue = new MessagesQueues();
        mThread = Thread.currentThread();
    }

    public void setMessageLogging(@Nullable Printer printer) {
        mLogging = printer;
    }

    public static void prepareMainLooper(){
        prepare();
        synchronized (Loopers.class) {
            if (sMainLooper != null) {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
            sMainLooper = MyLooper();
        }
    }

    public static Loopers getMainLooper() {
        synchronized (Loopers.class) {
            return sMainLooper;
        }
    }

    public static void prepare() {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("only one Looper");
        }
        sThreadLocal.set(new Loopers());
    }

    public static final Loopers MyLooper(){
        return sThreadLocal.get();
    }

    public static void loop() {
        Loopers me = MyLooper();
        MessagesQueues mQueue = me.mQueue;
        Log.d("HandlersTest", "start Loop,mQueue size:" + mQueue.size() + ",mQueue:" + mQueue);
        for (; ;) {
            try {
                Messages msg = mQueue.next(); // might block
                if (msg == null) {
                    continue;
                }
                if (msg.target != null) {
                    final Printer logging = me.mLogging;
                    if (logging != null) {
                        logging.println(">>>>> Dispatching to " + msg.target + " " +
                                msg.callback + ": " + msg.what);
                    }
                    msg.target.dispatchMessages(msg);
                    if (logging != null) {
                        logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
                    }
                }
                if (isQuit) {
                    Log.d("HandlersTest", "isQuit:" + isQuit);
                    return;
                }
            } catch (Exception e) {
                Log.d("HandlersTest", "Exception:" + e);
                e.printStackTrace();
            }
        }
    }

    public void quit() {
        isQuit = true;
        mQueue.quit();
        Log.d("HandlersTest", "quit,mQueue:"+mQueue);
    }
}
