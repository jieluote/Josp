package com.jieluote.josp.glide;

import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;

/**
 * 资源类(本demo只针对于网络数据)
 */
public class Resource {
    private final String TAG = Constant.TAG;
    private static Resource resource;

    private int acquired;   // 使用计数
    private ResourceListener listener;   // 监听
    private String key;    // key(由url路径转换而成)
    private Bitmap mBitmap;

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public int getAcquired() {
        return acquired;
    }

    public void setAcquired(int acquired) {
        this.acquired = acquired;
    }

    public ResourceListener getListener() {
        return listener;
    }

    public void setListener(ResourceListener listener) {
        this.listener = listener;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static Resource getInstance() {
        if (null == resource) {
            synchronized (Resource.class) {
                if (null == resource) {
                    resource = new Resource();
                }
            }
        }
        return resource;
    }

    public void acquire() {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new IllegalThreadStateException("Must call acquire on the main thread");
        }
        if (mBitmap.isRecycled()) {
            return;
        }
        ++acquired;
        Log.d(TAG, "resource acquire:" + acquired + ",resource:" + this.hashCode());
    }

    public void release() {
        if (acquired <= 0) {
            Log.d(TAG, "Cannot release a recycled or not yet acquired resource");
            return;
        }
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new IllegalThreadStateException("Must call release on the main thread");
        }
        if (--acquired == 0) {
            Log.d(TAG, "release equals zero,listener:"+listener);
            if (listener != null) {
                listener.onResourceReleased(key, this);
            }
        }
        Log.d(TAG, "resource release:" + acquired + ",resource:" + this.hashCode());
    }

    public void recycle() {
        if (acquired > 0) {        //引用计数大于0,不能被回收
            Log.d(TAG, "Cannot recycle a resource while it is still acquired");
            return;
        }
        if (mBitmap.isRecycled()) { // 已经被回收了
            Log.d(TAG, "Cannot recycle a resource that has already been recycled");
            return;
        }
        mBitmap.recycle();
        resource = null;
    }
}
