package com.jieluote.josp.threadPool;

/**
 * 执行器接口
 */
public interface Executor {
    //执行具体任务(runnable)
    void execute(Runnable command);
}
