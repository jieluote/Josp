package com.jieluote.josp.blockingQueue;

public interface BlockingQueue<E> {
    //入队
    void put(E item) throws InterruptedException;

    //出队
    E take() throws InterruptedException;

    //只是为了测试方便提供此方法,源码中没有此方法,而是用迭代器代替
    void printQueue();
}
