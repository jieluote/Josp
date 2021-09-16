package com.jieluote.josp.glide;

public interface ResourceListener {

    /**
     * 监听的方法（Resource不再使用了）
     *
     * @param key
     * @param resource
     */
    public void onResourceReleased(String key, Resource resource);
}
