package com.jieluote.josp.glide;

import android.util.Log;
import java.util.List;

/**
 * 解码任务(IO steam --> bitmap),是一个runnable
 */
public class DecodeJob implements Runnable, OnResponseListener {
    private final String TAG = Constant.TAG;
    String model;
    GlideContext glideContext;
    Callback resourceCallback;

    public DecodeJob(String model, GlideContext context) {
        this.glideContext = context;
        this.model = model;
    }

    public void setResourceCallback(Callback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    interface Callback {
        void onResourceReady(Resource resource);

        void onLoadFailed(Exception e);
    }

    @Override
    public void run() {
        Log.d(TAG, "DecodeJob run...");
        List<IModelLoader> modelLoaders = glideContext.getRegistry().getModelLoaders(model);
        if (modelLoaders == null || modelLoaders.size() == 0) {
            responseFailed(new Exception("no support modelLoaders"));
            return;
        }
        //获取到最后注册的那个loader
        IModelLoader iModelLoader = modelLoaders.get(modelLoaders.size() - 1);
        iModelLoader.loadData(model, this);
    }

    @Override
    public void responseSuccess(Resource resource) {
        if (resourceCallback != null) {
            Log.d(TAG, "DecodeJob success");
            resourceCallback.onResourceReady(resource);
        }
    }

    @Override
    public void responseFailed(Exception e) {
        if (resourceCallback != null) {
            Log.d(TAG, "DecodeJob failed");
            resourceCallback.onLoadFailed(e);
        }
    }
}
