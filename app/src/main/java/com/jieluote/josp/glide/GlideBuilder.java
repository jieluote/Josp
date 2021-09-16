package com.jieluote.josp.glide;

import com.jieluote.josp.glide.load.ActiveCache;
import com.jieluote.josp.glide.load.DiskLruCacheWrapper;
import com.jieluote.josp.glide.load.MemoryCache;
import com.jieluote.josp.glide.manager.RequestManagerRetriever;
import static com.jieluote.josp.glide.load.MemoryCache.MEMORY_MAX_SIZE;

public class GlideBuilder {
    private ActiveCache activeCache; // 活动缓存
    private MemoryCache memoryCache; // 内存缓存
    private DiskLruCacheWrapper diskLruCache; // 磁盘缓存
    private Engine engine;

    /**
     * 创建Glide
     * @return
     */
    public Glide build() {
        RequestManagerRetriever requestManagerRetriever = new RequestManagerRetriever();
        if (activeCache == null) {
            activeCache = new ActiveCache();
        }
        if (memoryCache == null) {
            memoryCache = new MemoryCache(MEMORY_MAX_SIZE);
        }
        if (diskLruCache == null) {
            diskLruCache = new DiskLruCacheWrapper();
        }

        if (engine == null) {
            engine = new Engine(activeCache, memoryCache, diskLruCache);
        }
        Glide glide = new Glide(requestManagerRetriever, engine);
        return glide;
    }
}
