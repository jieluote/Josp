package com.jieluote.josp.glide.load.request;

import android.util.Log;
import com.jieluote.josp.glide.Constant;
import com.jieluote.josp.glide.Engine;
import com.jieluote.josp.glide.Glide;
import com.jieluote.josp.glide.GlideContext;
import com.jieluote.josp.glide.Resource;
import androidx.annotation.Nullable;

/**
 * 具体的请求
 */
public class SingleRequest implements Request,RequestListener{
    private final String TAG = Constant.TAG;
    private Engine engine;
    private Object model;
    private Object target;
    private RequestListener targetListener;
    private GlideContext context;
    private Status status;
    private Resource resource;

    private enum Status {
        PENDING,
        RUNNING,
        COMPLETE,
        FAILED,
        CLEARED
    }

    public SingleRequest(Glide glide, Object model, Object target, RequestListener targetListener, GlideContext context) {
        this.engine = glide.getEngine();
        this.model = model;
        this.target = target;
        this.targetListener = targetListener;
        this.context = context;
        this.status = Status.PENDING;
    }

    @Override
    public void begin() {
        Log.d(TAG, "SingleRequest begin");
        engine.load(model, target, this, context);
        this.status = Status.RUNNING;
    }

    @Override
    public void clear() {
        Log.d(TAG, "SingleRequest clear");
        engine.release(resource);
        this.status = Status.CLEARED;
    }

    @Override
    public void recycle() {
        Log.d(TAG, "SingleRequest recycle");
        this.model = null;
        this.target = null;
        this.targetListener = null;
        this.context = null;
        this.resource = null;
    }

    private void cancel() {
    }

    @Override
    public boolean onLoadFailed(@Nullable Exception e, Object model, Object target) {
        this.status = Status.FAILED;
        resource = (Resource) model;
        return targetListener.onLoadFailed(e, model, target);
    }

    @Override
    public boolean onResourceReady(Resource resource, Object model, Object target) {
        this.status = Status.COMPLETE;
        this.resource = resource;
        return targetListener.onResourceReady(resource, model, target);
    }

    @Override
    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    @Override
    public boolean isComplete() {
        return status == Status.COMPLETE;
    }

    @Override
    public boolean isCleared() {
        return status == Status.CLEARED;
    }

    @Override
    public boolean isFailed() {
        return status == Status.FAILED;
    }
}
