package com.jieluote.josp.glide.manager;

import android.content.Context;
import android.util.Log;
import com.jieluote.josp.glide.Constant;
import com.jieluote.josp.glide.Glide;
import com.jieluote.josp.glide.LifecycleListener;
import com.jieluote.josp.glide.RequestBuilder;
import com.jieluote.josp.glide.load.request.Request;
import com.jieluote.josp.glide.load.request.RequestTracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 与生命周期绑定,管理Request
 */
public class RequestManager implements LifecycleListener {
    private final String TAG = Constant.TAG;
    private final RequestTracker requestTracker;
    private Context context;
    private Glide glide;

    public RequestManager(Context context) {
        this(null,context);
    }

    public RequestManager(Glide glide, Context context) {
        this.glide = glide;
        this.context = context;
        requestTracker = new RequestTracker();
    }

    public RequestBuilder load(@Nullable String string) {
        Log.d(TAG, "RequestManager start load");
        RequestBuilder builder = new RequestBuilder(glide, this, glide.getGlideContext());
        return builder.load(string);
    }

    public void track(@NonNull Request request) {
        requestTracker.runRequest(request);
    }

    @Override
    public void onStart() {
        requestTracker.resumeRequests();
    }

    @Override
    public void onStop() {
        requestTracker.pauseRequests();
    }

    @Override
    public void onDestroy() {
        requestTracker.clearRequests();
    }
}
