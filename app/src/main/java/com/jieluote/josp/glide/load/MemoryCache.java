package com.jieluote.josp.glide.load;

import android.graphics.Bitmap;
import android.os.Build;
import com.jieluote.josp.glide.MemoryCacheCallback;
import com.jieluote.josp.glide.Resource;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

/**
 * 内存缓存--继承自LruCache(线程安全,基于LinkedHashMap)
 */
public class MemoryCache extends LruCache<String, Resource> {
    public final static int MEMORY_MAX_SIZE = 1024 * 1024 * 60;

    public Resource removeResource(String key) {
        Resource resource = remove(key);
        return resource;
    }

    private MemoryCacheCallback memoryCacheCallback;

    public void setMemoryCacheCallback(MemoryCacheCallback memoryCacheCallback) {
        this.memoryCacheCallback = memoryCacheCallback;
    }

    /**
     * 传入元素最大值，给LruCache
     *
     * @param maxSize
     */
    public MemoryCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull Resource resource) {
        // return super.sizeOf(key, value);
        Bitmap bitmap = resource.getBitmap(); // 8

        // 最开始的时候
        // int result = bitmap.getRowBytes() * bitmap.getHeight();
        // API 12  3.0
        // result = bitmap.getByteCount(); // 在bitmap内存复用上有区别 （所属的）
        // API 19 4.4
        // result = bitmap.getAllocationByteCount(); // 在bitmap内存复用上有区别 （整个的）

        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        }

        return bitmap.getByteCount();
    }

    /**
     * 1.重复的key
     * 2.最少使用的元素会被移除
     *
     * @param evicted
     * @param key
     * @param oldResource
     * @param newResource
     */
    @Override
    protected void entryRemoved(boolean evicted, @NonNull String key, @NonNull Resource oldResource, @Nullable Resource newResource) {
        super.entryRemoved(evicted, key, oldResource, newResource);
        if (memoryCacheCallback != null) {
            memoryCacheCallback.entryRemovedMemoryCache(key, oldResource);
        }
    }
}
