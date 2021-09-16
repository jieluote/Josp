package com.jieluote.josp.threadPool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import com.jieluote.josp.threadPool.ScheduledThreadPoolExecutor.DelayedWorkQueue;

/**
 * 执行器,提供常见的4种线程池实现
 */
public class Executors {

    public static Executor newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    public static Executor newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    //这里要注意下,singleThread后缀是Executor和其它三者都不一样
    public static Executor newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    //newScheduledThreadPool 这里仅提供结构展示,没有具体实现
    private static Executor newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize, Integer.MAX_VALUE,
                10L, TimeUnit.MILLISECONDS,
                new DelayedWorkQueue());
    }
}
