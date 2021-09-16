package com.jieluote.josp.handler;

import android.os.SystemClock;
import android.util.Log;
import java.util.ArrayList;

public class MessagesQueues {
    private ArrayList<Messages> queues = new ArrayList<Messages>(); //用list模拟链表 源码中messageQueue是单向链表结构
    private Messages mMessages;
    private boolean mQuitting;
    private boolean mBlocked;

    public void enqueueMessage(Messages msg, long when) {
        Log.d("HandlersTest", "enqueueMessage:" + msg);
        synchronized (this) {
            msg.when = when;
            Messages p = mMessages;

            //如果新的msg延时时间比旧的小,那么把它插入到链表头
            if (p == null || when == 0 || when < p.when) {
                mMessages = msg;
                queues.add(0, msg);
            } else {
                queues.add(msg);
            }
            //如果之前的阻塞了,那么只要来了新的msg都将唤醒
            if (mBlocked) {
                //源码中通过nativeWake(mPtr)唤醒
                this.notify();
            }
        }
    }

    public Messages next() throws InterruptedException {
        final long now = SystemClock.uptimeMillis();
        synchronized (this) {
            if (mQuitting) {
                return null;
            }
            if (queues.isEmpty()) {
                return null;
            }
            Messages msg = queues.get(0);//总是取链表头的数据,因为它的延时时间最短
            if (msg != null) {
                //如果还没到执行时间,就阻塞
                if (now < msg.when) {
                    int nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
                    Log.d("HandlersTest", "nextPollTimeoutMillis:" + nextPollTimeoutMillis / 1000L);
                    mBlocked = true;
                    //源码中通过native方法进行阻塞 nativePollOnce(ptr, nextPollTimeoutMillis);
                    this.wait(nextPollTimeoutMillis);
                }
            }
            queues.remove(msg);
            return msg;
        }
    }

    public int size() {
        return queues.size();
    }

    public void clear(){
        if(queues!=null){
            queues.clear();
        }
    }

    public void quit(){
        synchronized (this) {
            if (mQuitting) {
                return;
            }
            mQuitting = true;
            clear();
            mMessages = null;
        }
    }
}
