package com.jieluote.josp.glide;

public interface MemoryCacheCallback {

    /**
     * @param key
     * @param oldResource
     */
    public void entryRemovedMemoryCache(String key, Resource oldResource);
}
