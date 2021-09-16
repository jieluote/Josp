package com.jieluote.josp.glide.load;

import android.content.Context;
import com.jieluote.josp.glide.OnResponseListener;
import com.jieluote.josp.glide.Resource;

/**
 * 加载外部资源
 */
public interface ILoadData {
    Resource loadResource(String path, OnResponseListener onResponseListener, Context context);
}
