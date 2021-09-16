package com.jieluote.josp.glide;

import android.graphics.Bitmap;

public interface BitmapPool {
    void put(Bitmap bitmap);

    Bitmap get(int width, int height, Bitmap.Config config);

    void clearMemory();
}
