package com.jieluote.josp.blockingQueue;

import android.os.Bundle;
import android.util.Log;

import com.jieluote.josp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BlockingQueue测试类,涉及到的知识点:
 * 1.模拟实现ArrayBlockingQueue阻塞队列(V1-V3三种不同的实现,从简到难)
 * 2.synchronized + wait/notify的用法
 * 3.ReentrantLock的用法
 * 4.Thread.join()的用法
 * 5.生产者消费者模型
 */
public class BlockingQueueActivity extends AppCompatActivity {
    public static final String TAG = "BlockingQueueTest";
    private BlockingQueue<Integer> queue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blockingqueue_activity);
        //testTakeBlocking();
        testPutBlocking();
    }

    class ProducerThread extends Thread {
        public ProducerThread(String threadName) {
            super(threadName);
        }
        boolean isSlow = false;

        void setSlow(boolean isSlow) {
            this.isSlow = isSlow;
        }

        @Override
        public void run() {
            super.run();
            Log.d(TAG, Thread.currentThread().getName() + " 开始生产");
            for (int i = 0; i < 15; i++) {
                try {
                    if (isSlow) {
                        sleep(2000);
                    }
                    Log.d(TAG, Thread.currentThread().getName() + " put:" + i);
                    queue.put(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, Thread.currentThread().getName() + " 结束生产");
        }
    }

    class ConsumerThread extends Thread {
        public ConsumerThread(String threadName) {
            super(threadName);
        }

        boolean isSlow = false;
        void setSlow(boolean isSlow) {
            this.isSlow = isSlow;
        }

        @Override
        public void run() {
            super.run();
            Log.d(TAG, Thread.currentThread().getName() + " 开始消费");
            for (int i = 0; i < 10; i++) {
                try {
                    if(isSlow){
                        sleep(2000);
                    }
                    Log.d(TAG, Thread.currentThread().getName() + " take:" + queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, Thread.currentThread().getName() + " 结束消费");
        }
    }

    /**
     * 测试消费时阻塞(消费大于生产速度,没得拿)
     */
    private void testTakeBlocking() {
        testBlocking(true, false);
    }

    /**
     * 测试生产时阻塞(生产大于消费速度,装不下)
     */
    private void testPutBlocking() {
        testBlocking(false, true);
    }

    private void testBlocking(boolean producerSlow,boolean consumerSlow) {
        //这里可切换三种ArrayBlockingQueue不同的实现
        //queue = new ArrayBlockingQueueV1<Integer>(10);
        //queue = new ArrayBlockingQueueV2<Integer>(10);
        queue = new ArrayBlockingQueueV3<Integer>(10);

        List<Thread> threadList = new ArrayList<Thread>();
        //这里消费者和生产者各只有一个线程,是为了更清晰直观的查看两者交互过程,也可以用for循环或者线程池创建多个
        ProducerThread producerThread = new ProducerThread("生产者");
        producerThread.setSlow(producerSlow);
        producerThread.start();

        ConsumerThread consumerThread = new ConsumerThread("消费者");
        consumerThread.setSlow(consumerSlow);
        consumerThread.start();

        threadList.add(producerThread);
        threadList.add(consumerThread);

        for (int i = 0; i < threadList.size(); i++) {
            try {
                //等list中的两个子线程都执行完毕才继续向下执行主线程
                threadList.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //查看queue里剩余的元素
        Log.d(TAG, "剩余元素:");
        queue.printQueue();
    }

}
