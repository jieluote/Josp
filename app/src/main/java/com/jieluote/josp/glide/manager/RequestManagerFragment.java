package com.jieluote.josp.glide.manager;

import android.annotation.SuppressLint;

import com.jieluote.josp.glide.Constant;
import com.jieluote.josp.glide.LifecycleListener;

import android.app.Fragment;
import android.util.Log;

/**
 * Activity生命周期关联管理
 */
public class RequestManagerFragment extends Fragment {
    private final String TAG = Constant.TAG;
    private RequestManager requestManager;

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public RequestManagerFragment() {
    }

    private LifecycleListener lifecycleListener;

    @SuppressLint("ValidFragment")
    public RequestManagerFragment(LifecycleListener lifecycleListener) {
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"Activity onStart");
        if (requestManager != null) {
            requestManager.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"Activity onStop");
        if (requestManager != null) {
            requestManager.onStop();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Activity onDestroy");
        super.onDestroy();
        if (requestManager != null) {
            requestManager.onDestroy();
        }
    }
}
