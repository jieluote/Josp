package com.jieluote.josp.glide;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;
import java.util.TreeMap;

public class LruBitmapPool extends LruCache<Integer,Bitmap> implements BitmapPool{
    private final String TAG = Constant.TAG;
    private static final Bitmap.Config DEFAULT_CONFIG = Bitmap.Config.ARGB_8888;
    private TreeMap<Integer, String> treeMap = new TreeMap<>();
    private static final int MAX_SIZE_MULTIPLE = 8;

/**
 * ALPHA_8  只有透明度                             共8位  1字节
 * RGB_565  Red 5位,Green 6,Blue 5位              共16位 2字节 无透明度
 * ARGB_4444 Alpha 4位,Red 4位,Green 4,Blue 4位   共16位 2字节
 * ARGB_8888 Alpha 8位,Red 8位,Green 8,Blue 8位   共32位 4字节
  **/

    public LruBitmapPool(int maxSize) {
        super(maxSize);
    }

    @Override
    public void put(Bitmap bitmap) {
        if (bitmap == null) {
            Log.d(TAG, "LruBitmapPool bitmap is null,return");
            return;
        }
        //bitmap必须是不可变的
        if (!bitmap.isMutable()) {
            Log.d(TAG, "LruBitmapPool bitmap must be immutable,return");
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return;
        }
        int size = getSize(bitmap);
        //图片大小不能超过复用池大小
        if (size > maxSize()) {
            Log.d(TAG, "LruBitmapPool bitmap size must small than maxSize,return");
            return;
        }
        put(size, bitmap);
        treeMap.put(size, null);
    }

    @Override
    public Bitmap get(int width, int height, Bitmap.Config config) {
        int bitmapSize = width * height * (config == DEFAULT_CONFIG ? 4 : 2);
        //从临时缓存中找到大于等于bitmapSize的
        Integer possibleSize = treeMap.ceilingKey(bitmapSize);
        if (possibleSize == null) {
            return null;
        }
        // 查找容器取出来的size，需要小于计算出来的 (getSize * MAX_SIZE_MULTIPLE)
        if (possibleSize <= (bitmapSize * MAX_SIZE_MULTIPLE)) {
            Bitmap remove = remove(possibleSize);
            return remove;
        }
        return null;
    }

    private int getSize(Bitmap bmp){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            return bmp.getAllocationByteCount();
        }
        return bmp.getByteCount();
    }

    @Override
    protected int sizeOf(Integer key, Bitmap value) {
        return getSize(value);
    }

    @Override
    protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
    }

    @Override
    public void clearMemory() {

    }
}
