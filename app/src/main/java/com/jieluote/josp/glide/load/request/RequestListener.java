package com.jieluote.josp.glide.load.request;

import android.graphics.Bitmap;

import com.jieluote.josp.R;
import com.jieluote.josp.glide.Resource;


import androidx.annotation.Nullable;

public interface RequestListener {
    boolean onLoadFailed(
            @Nullable Exception e, Object model, Object target);

    boolean onResourceReady(
            Resource resource, Object model, Object target);
}
