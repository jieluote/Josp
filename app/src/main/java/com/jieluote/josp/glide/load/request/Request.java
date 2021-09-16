package com.jieluote.josp.glide.load.request;

public interface Request {
    void begin();

    void clear();

    boolean isRunning();

    boolean isComplete();

    boolean isCleared();

    boolean isFailed();

    void recycle();
}
