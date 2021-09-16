package com.jieluote.josp.glide;

import android.widget.ImageView;
import com.jieluote.josp.glide.load.request.Request;
import com.jieluote.josp.glide.load.request.RequestListener;
import com.jieluote.josp.glide.load.request.SingleRequest;
import com.jieluote.josp.glide.manager.RequestManager;

/**
 * 构建Request
 */
public class RequestBuilder {
    private Object model;
    private Glide glide;
    private GlideContext glideContext;
    private RequestManager requestManager;

    public RequestBuilder(Glide glide, RequestManager requestManager, GlideContext glideContext) {
        this.glide = glide;
        this.glideContext = glideContext;
        this.requestManager = requestManager;
    }

    public RequestBuilder load(Object model) {
        this.model = model;
        return this;
    }

    private Request buildRequest(Object target, RequestListener targetListener) {
        return new SingleRequest(glide, model, target, targetListener, glideContext);
    }

    public void into(ImageView imageView) {
        into(imageView, null);
    }

    public void into(ImageView imageView, RequestListener targetListener) {
        Request request = buildRequest(imageView, targetListener);
        requestManager.track(request);
    }
}
