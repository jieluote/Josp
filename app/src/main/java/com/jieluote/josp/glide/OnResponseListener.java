package com.jieluote.josp.glide;

public interface OnResponseListener {
    void responseSuccess(Resource resource);

    void responseFailed(Exception e);
}
