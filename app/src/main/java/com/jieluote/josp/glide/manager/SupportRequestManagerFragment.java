package com.jieluote.josp.glide.manager;

import android.annotation.SuppressLint;
import android.util.Log;

import com.jieluote.josp.glide.Constant;
import com.jieluote.josp.glide.LifecycleListener;

import androidx.fragment.app.Fragment;

/**
 * FragmentActivity/Fragment 生命周期关联管理
 */
public class SupportRequestManagerFragment extends Fragment {
    private final String TAG = Constant.TAG;
    private RequestManager requestManager;

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public SupportRequestManagerFragment() {
    }

    private LifecycleListener lifecycleListener;

    @SuppressLint("ValidFragment")
    public SupportRequestManagerFragment(LifecycleListener lifecycleListener) {
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"fragment onStart");
        if (requestManager != null) {
            requestManager.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"fragment onStop");
        if (requestManager != null) {
            requestManager.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"fragment onDestroy");
        if (requestManager != null) {
            requestManager.onDestroy();
        }
    }
}
