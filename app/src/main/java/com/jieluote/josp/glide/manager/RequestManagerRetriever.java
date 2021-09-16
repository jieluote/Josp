package com.jieluote.josp.glide.manager;

import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import com.jieluote.josp.glide.Glide;
import com.jieluote.josp.glide.util.Utils;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * 建立隐藏的Fragment和RequestManager绑定
 * 建立隐藏的Fragment好处：
 * 1.屏蔽细节,不管Glide在哪种载体上使用,其生命周期只跟着Fragment走
 * 2.不管传入哪种Context都可以知道它的生命周期
 */
public class RequestManagerRetriever {
    static final String FRAGMENT_TAG = "com.jieluote.glide.manager";

    @NonNull
    public RequestManager get(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("You cannot start a load on a null Context");
        } else if (Utils.isOnMainThread() && !(context instanceof Application)) {
            if (context instanceof FragmentActivity) {
                return get((FragmentActivity) context);
            } else if (context instanceof Activity) {
                return get((Activity) context);
            } else if (context instanceof ContextWrapper) {
                return get(((ContextWrapper) context).getBaseContext());
            }
        }
        return new RequestManager(context);
    }

    public RequestManager get(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        return fragmentGet(activity, fm);
    }

    public RequestManager get(FragmentActivity fragmentActivity) {
        return supportFragmentGet(fragmentActivity,fragmentActivity.getSupportFragmentManager());
    }

    public RequestManager get(Fragment fragment) {
        androidx.fragment.app.FragmentManager fm = fragment.getChildFragmentManager();
        return supportFragmentGet(fragment.getActivity(), fm);
    }

    private RequestManager fragmentGet(@NonNull Context context,
                                       @NonNull android.app.FragmentManager fm) {
        Glide glide = Glide.get(context);
        RequestManagerFragment current = getRequestManagerFragment(fm);
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            requestManager = new RequestManager(glide,context);
            current.setRequestManager(requestManager);
        }
        return requestManager;
    }

    @NonNull
    private RequestManager supportFragmentGet(
            @NonNull Context context,
            @NonNull androidx.fragment.app.FragmentManager fm) {
        SupportRequestManagerFragment current =
                getSupportRequestManagerFragment(fm);
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            Glide glide = Glide.get(context);
            requestManager = new RequestManager(glide,context);
            current.setRequestManager(requestManager);
        }
        return requestManager;
    }

    private RequestManagerFragment getRequestManagerFragment(
            @NonNull final android.app.FragmentManager fm) {
        RequestManagerFragment current = (RequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
                current = new RequestManagerFragment();
                fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
            }
        return current;
    }

    private SupportRequestManagerFragment getSupportRequestManagerFragment(
            @NonNull final androidx.fragment.app.FragmentManager fm) {
        SupportRequestManagerFragment current =
                (SupportRequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
            current = new SupportRequestManagerFragment();
            fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
        }
        return current;
    }
}
