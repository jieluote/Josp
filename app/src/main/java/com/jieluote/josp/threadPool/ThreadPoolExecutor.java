package com.jieluote.josp.threadPool;

import android.util.Log;

import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程池的内部实现类
 * 线程池整体的运行流程做个形象的比喻:
 * 一个生产商品的公司,接了10个订单(task),刚好有10个正式员工(核心线程),
 * 那么这10个人就人手一个订单去生产商品,这时又来了5个订单,因为每个人手上
 * 都有活没人能来接手,只能先放到仓库(阻塞队列)等待有人空下来处理(线程复用)。
 * 可不一会又来了10个订单,仓库都放不下了(队列满了),老板一看这不行呀,赶快招了10个
 * 临时工(非核心线程)来帮忙。但是因为是订单旺季,很快又来了10个订单,
 * 仓库放不下,正式员工和临时工也都没空闲的,没办法了,这部分订单只能舍弃了(拒绝策略)。
 * 接下来就看正式工和临时工们的干活速度了,谁把手头中的活先干完,谁就去仓库拿订单接着做。
 * 当临时工把自己的订单做完后并且仓库里也没有可做的,那么公司已经不需要他了,可以回家了(线程回收)
 * 而正式工还是会在公司,即使订单已做完还是处于随时待命的状态。
 */
public class ThreadPoolExecutor implements Executor {
    public static final String TAG = "ThreadPoolExecutor";
    private volatile int corePoolSize;     //核心线程数
    private volatile int maximumPoolSize;  //最大线程数
    private int workThreadSize;            //工作线程数
    private final HashSet<Worker> workers = new HashSet<>();
    private final BlockingQueue<Runnable> workQueue;
    private volatile long keepAliveTime;
    private volatile RejectedExecutionHandler handler; //拒绝策略

    private volatile boolean allowCoreThreadTimeOut;  //是否允许核心线程超时回收

    private static final RejectedExecutionHandler defaultHandler =
            new AbortPolicy();

    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                defaultHandler);
    }

    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.handler = handler;
    }

    public void allowCoreThreadTimeOut(boolean value) {
        if (value && keepAliveTime <= 0)
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            if (value){
                interruptIdleWorkers();
            }
        }
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        //分为四种情况
        //1.当工作线程小于核心线程数,创建核心线程
        if(workThreadSize < corePoolSize){
            if(addWorker(command,true)) {
                Log.d(TAG, "execute --> add core worker");
                return;
            }
        }

        //2.否则,当队列未满放入队列
        else if(workQueue.offer(command)){
            Log.d(TAG,"execute --> add to workQueue:"+ workQueue.getClass().getName());
        }

        //3.否则,创建非核心线程数(不能大于maxSize)
        else if (addWorker(command, false)){
            Log.d(TAG,"execute --> add non-core worker");

        //4.否则 拒绝
        }else{
            Log.d(TAG,"execute --> reject");
            reject(command);
        }
    }

    /**
     * @param firstTask 任务
     * @param isCore 是否是核心线程
     * @return boolean
     */
    private boolean addWorker(Runnable firstTask, boolean isCore) {
        for (; ;) {
            if (firstTask == null) {
                return false;
            }
            if (workThreadSize >= (isCore ? corePoolSize : maximumPoolSize)) {
                return false;
            }
            Worker w = new Worker(firstTask);
            final Thread t = w.thread;
            t.start();
            workers.add(w);
            workThreadSize++;
            return true;
        }
    }

    /**
     * 最终干活的工作线程,名字也很贴切:工人类
     * 继承AQS(这里并未实现相关操作)和实现runnable接口
     * 是线程池的核心,线程池通过创建工作线程,然后
     * 在其run方法里调用任务task的run方法达到多线程运行的目的
     * 一个工作线程可以处理多个任务
     */
    private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
        final Thread thread;
        Runnable firstTask;

        Worker(Runnable firstTask) {
            this.firstTask = firstTask;
            this.thread = new Thread(this);
        }

        @Override
        public void run() {
            Runnable task = firstTask;
            firstTask = null;
            Log.d(TAG, "run worker:" + this.hashCode());
            //在工作线程中,首先会执行由构造函数传进来的任务(这也是为什么叫做firstTask的原因)
            //然后才会去队列里拿任务执行
            while (task != null || (task = getTask()) != null) {
                /*线程池中线程复用的奥秘就在这个while循环中,其实就是不断的从队列中
                 *拿任务来执行(getTask方法),当任务为空了,工作线程(非核心)才结束
                 *也就是说一个工作线程能执行多个任务,达到了线程复用的目的 */
                task.run();
                task = null;
            }
            workThreadSize--;
            workers.remove(this);
        }
    }

    private Runnable getTask() {
        boolean timed = allowCoreThreadTimeOut || workThreadSize > corePoolSize;
        Runnable runnable = null;
        try {
            /*核心线程和非核心线程的区别在这里体现
             *如果不设置allowCoreThreadTimeOut为true,那么核心线程会阻塞,一直会运行
             *而非核心线程会被回收 */
            if (timed) {
                //keepAliveTime时间段内获取任务,否则返回null,相当于线程结束了被回收
                runnable = workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS);
            } else {
                //获取任务,如果没有,则阻塞,相当于线程一直在运行等待执行任务
                runnable = workQueue.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return runnable;
    }

    public static class AbortPolicy implements RejectedExecutionHandler {
        public AbortPolicy() {
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RuntimeException("Task " + r.toString() +
                    " rejected from " +
                    e.toString());
        }
    }

    final void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    private void interruptIdleWorkers() {
        for (Worker w : workers) {
            Thread t = w.thread;
            if (!t.isInterrupted()) {
                t.interrupt();
            }
        }
    }

    public void shutdown() {
        interruptIdleWorkers();
    }

}
