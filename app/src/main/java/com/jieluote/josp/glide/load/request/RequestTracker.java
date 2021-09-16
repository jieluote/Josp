package com.jieluote.josp.glide.load.request;

import android.util.Log;

import com.jieluote.josp.glide.Constant;
import com.jieluote.josp.glide.util.Utils;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 集中管理Request
 */
public class RequestTracker {
    private final String TAG = Constant.TAG;
    private final Set<Request> requests =
            Collections.newSetFromMap(new WeakHashMap<Request, Boolean>());
    private boolean isPaused;

    public void runRequest(Request request) {
        Log.d(TAG,"RequestTracker runRequest");
        requests.add(request);
        if (!isPaused) {
            request.begin();
        } else {
            request.clear();
        }
    }

    public void resumeRequests() {
        Log.d(TAG, "RequestTracker resumeRequests");
        isPaused = false;
        for (Request request : Utils.getSnapshot(requests)) {
            if (!request.isComplete() && !request.isRunning()) {
                request.begin();
            }
        }
    }

    public void pauseRequests() {
        Log.d(TAG, "RequestTracker pauseRequests");
        isPaused = true;
        for (Request request : Utils.getSnapshot(requests)) {
            if (request.isRunning()) {
                request.clear();
            }
        }
    }

    public void clearRequests() {
        Log.d(TAG, "RequestTracker clearRequests");
        for (Request request : Utils.getSnapshot(requests)) {
            request.clear();
            request.recycle();
        }
    }
}
