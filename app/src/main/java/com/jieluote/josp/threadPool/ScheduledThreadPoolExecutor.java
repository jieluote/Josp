package com.jieluote.josp.threadPool;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor {
    public ScheduledThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ScheduledThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    /**
     * 可延时执行的阻塞队列,ScheduledThreadPool定时和延时的功能依靠此队列实现
     * DelayedWorkQueue中主要用到了Lock的condition await(time) 超时等待来实现定时功能
     * 因为不是理解线程池的重点,所以这里暂不提供实现,知道整体的结构关系即可
     */
    static class DelayedWorkQueue extends AbstractQueue<Runnable>
            implements BlockingQueue<Runnable> {

        @NonNull
        @Override
        public Iterator<Runnable> iterator() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void put(Runnable runnable) throws InterruptedException {

        }

        @Override
        public boolean offer(Runnable runnable, long l, TimeUnit timeUnit) throws InterruptedException {
            return false;
        }

        @Override
        public Runnable take() throws InterruptedException {
            return null;
        }

        @Override
        public Runnable poll(long l, TimeUnit timeUnit) throws InterruptedException {
            return null;
        }

        @Override
        public int remainingCapacity() {
            return 0;
        }

        @Override
        public int drainTo(Collection<? super Runnable> collection) {
            return 0;
        }

        @Override
        public int drainTo(Collection<? super Runnable> collection, int i) {
            return 0;
        }

        @Override
        public boolean offer(Runnable runnable) {
            return false;
        }

        @Nullable
        @Override
        public Runnable poll() {
            return null;
        }

        @Nullable
        @Override
        public Runnable peek() {
            return null;
        }
    }
}
