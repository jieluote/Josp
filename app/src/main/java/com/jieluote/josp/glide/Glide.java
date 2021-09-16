package com.jieluote.josp.glide;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.jieluote.josp.glide.load.HttpUrlLoader;
import com.jieluote.josp.glide.manager.RequestManager;
import com.jieluote.josp.glide.manager.RequestManagerRetriever;

import androidx.fragment.app.FragmentActivity;

public class Glide {
    private final String TAG = Constant.TAG;
    private RequestManagerRetriever retriever;
    private static volatile Glide glide;
    private final Engine engine;
    private final Registry registry;
    private final GlideContext glideContext;
    private final BitmapPool bitmapPool;

    public Glide(RequestManagerRetriever retriever, Engine engine) {
        Log.d(TAG, "Glide init");
        this.retriever = retriever;
        this.engine = engine;
        registry = new Registry();
        bitmapPool = new LruBitmapPool(1024 * 1024 * 6); //这里的size随便写了下,实际上源码里的是由屏幕像素,系统版本等多个变量计算得出的
        registry.append(new HttpUrlLoader(bitmapPool));
        glideContext = new GlideContext(registry, engine);
    }

    public Engine getEngine() {
        return engine;
    }

    public GlideContext getGlideContext() {
        return glideContext;
    }

    public static Glide get(Context context) {
        if (glide == null) {
            synchronized (Glide.class) {
                if (glide == null) {
                    checkAndInitializeGlide(context);
                }
            }
        }
        return glide;
    }

    private static void checkAndInitializeGlide(Context context) {
        glide = new GlideBuilder().build();
    }

    public RequestManagerRetriever getRetriever() {
        return retriever;
    }

    /**
     * with()主要是
     * 1.获得RequestManager对象
     * 2.根据我们传入的Context参数来确定图片加载的生命周期
     */

    public static RequestManager with(FragmentActivity fragmentActivity) {
        return getRetriever(fragmentActivity).get(fragmentActivity);
    }

    public static RequestManager with(Activity activity) {
        return getRetriever(activity).get(activity);
    }

    public static RequestManager with(Context mContext) {
        return getRetriever(mContext).get(mContext);
    }

    public static RequestManagerRetriever getRetriever(Context context) {
        return Glide.get(context).getRetriever();
    }

}
