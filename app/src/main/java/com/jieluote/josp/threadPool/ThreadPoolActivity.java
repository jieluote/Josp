package com.jieluote.josp.threadPool;

import android.os.Bundle;
import android.util.Log;
import com.jieluote.josp.R;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 线程池测试类,涉及到的知识点:
 * 1.线程池的内部实现
 * 2.各个线程池的差异
 * 3.阻塞队列的使用
 */
public class ThreadPoolActivity extends AppCompatActivity {
    public static final String TAG = "ThreadPoolTest";
    public static final int TASK_COUNT = 30; //任务数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.threadpool_activity);
        testCustomThreadPool();
        //testOriginalThreadPool();
    }

    /**
     * 测试我们自己实现的线程池
     */
    private void testCustomThreadPool(){
        Log.d(TAG, "start testCustomThreadPool");
        //这里我们可以比较下使用不同的线程池,其结果有何差异(可以从线程数量、完成时间来比较)
        //newCachedThreadPool工作线程多,吞吐量大,速度快,但需要注意资源消耗
        Executor executor = Executors.newCachedThreadPool();

        //newFixedThreadPool,固定数量线程池,速度位于中间
        //Executor executor = Executors.newFixedThreadPool(5);

        //newSingleThreadExecutor,单线程池,速度最慢
        //Executor executor = Executors.newSingleThreadExecutor();

        for (int i = 0; i < TASK_COUNT; i++) {
            int index = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //模拟任务耗时
                        Thread.sleep(500);
                        Log.d(TAG, "custom task index:" + index + ",thread name:" + Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 测试JUC中的线程池
     */
    private void testOriginalThreadPool() {
        Log.d(TAG, "start testOriginalThreadPool");
        java.util.concurrent.Executor executor = java.util.concurrent.Executors.newCachedThreadPool();
        //java.util.concurrent.Executor executor = java.util.concurrent.Executors.newFixedThreadPool(5);
        //java.util.concurrent.Executor executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        for (int i = 0; i < TASK_COUNT; i++) {
            int index = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //模拟任务耗时
                        Thread.sleep(500);
                        Log.d(TAG, "original task index:" + index + ",thread name:" + Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}