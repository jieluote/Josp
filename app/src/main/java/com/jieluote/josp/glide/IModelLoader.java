package com.jieluote.josp.glide;

public interface IModelLoader {
    Resource loadData(String path, OnResponseListener onResponseListener);
    boolean handles(Object model);
}
