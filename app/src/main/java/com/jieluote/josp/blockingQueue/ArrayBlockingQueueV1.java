package com.jieluote.josp.blockingQueue;

import android.util.Log;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 模拟ArrayBlockingQueue, 基于synchronized + wait/notify
 * (wait/notify已不推荐使用)
 * @param <E>
 */
public class ArrayBlockingQueueV1<E> implements BlockingQueue<E>{
    private final Object[] items;
    private int count;
    private int putIndex;
    private int takeIndex;

    public ArrayBlockingQueueV1(int capacity) {
        Log.d(BlockingQueueActivity.TAG, "初始化阻塞队列(synchronized实现),容量为:"+capacity);
        items = new Object[capacity];
    }

    @Override
    public void put(E item) throws InterruptedException {
        synchronized (this) {
            while (count == items.length) {
                Log.d(BlockingQueueActivity.TAG, Thread.currentThread().getName() + " put " + item + "时队列满,阻塞");
                this.wait();
            }
            enqueue(item);
            this.notify();
        }
    }

    @Override
    public E take() throws InterruptedException {
        synchronized (this) {
            while (count == 0) {
                Log.d(BlockingQueueActivity.TAG, Thread.currentThread().getName() + " take时队列空,阻塞");
                this.wait();
            }
            this.notify();
            return dequeue();
        }
    }

    private void enqueue(E item) {
        items[putIndex] = item;
        if (++putIndex == items.length) {
            putIndex = 0;
        }
        count++;
    }

    private E dequeue() {
        Object e = items[takeIndex];
        items[takeIndex] = null;
        if (++takeIndex == items.length) {
            takeIndex = 0;
        }
        count--;
        return (E) e;
    }

    @Override
    public void printQueue() {
        for (Object i : items) {
            if (i != null) {
                Log.d(BlockingQueueActivity.TAG, "item:" + i);
            }
        }
    }
}
