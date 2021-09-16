package com.jieluote.josp.blockingQueue;

import android.util.Log;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 模拟ArrayBlockingQueue, 基于Lock实现(单Condition)
 * @param <E>
 */
public class ArrayBlockingQueueV2<E> implements BlockingQueue<E>{
    private final Object[] items;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private int count;
    private int putIndex;
    private int takeIndex;

    public ArrayBlockingQueueV2(int capacity) {
        Log.d(BlockingQueueActivity.TAG, "初始化阻塞队列(单condition实现),容量为:"+capacity);
        items = new Object[capacity];
    }

    @Override
    public void put(E item) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length) {
                Log.d(BlockingQueueActivity.TAG, Thread.currentThread().getName() + " put " + item + "时队列满,阻塞");
                condition.await();
            }
            enqueue(item);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                Log.d(BlockingQueueActivity.TAG, Thread.currentThread().getName() + " take时队列空,阻塞");
                condition.await();
            }
            condition.signalAll();
        } finally {
            lock.unlock();
        }
        return dequeue();
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
