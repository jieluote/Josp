package com.jieluote.josp.handler;

public class Messages {
    public int what;
    public Object obj;
    public Runnable callback;
    public Handlers target;
    public long when;

    public Messages() {
    }

    public Messages(Handlers target) {
        this.target = target;
    }

    public Runnable getCallback() {
        return callback;
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }


    @Override
    public String toString() {
        return "Messages{" +
                "target=" + target +
                ", what=" + what +
                ", object=" + obj +
                '}';
    }
}
