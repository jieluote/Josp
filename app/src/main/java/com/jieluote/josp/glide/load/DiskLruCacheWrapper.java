package com.jieluote.josp.glide.load;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import com.jieluote.josp.glide.Resource;
import com.jieluote.josp.glide.load.disklrucache.DiskLruCache;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 对DiskLruCache的包装，更方便使用
 */
public class DiskLruCacheWrapper {
    private final String TAG = DiskLruCacheWrapper.class.getSimpleName();
    private final String DISKLRU_CACHE_DIR = "disk_lru_cache_dir"; // 磁盘缓存的的目录

    private final int APP_VERSION = 1; // 版本号，一旦修改这个版本号，之前的缓存失效
    private final int VALUE_COUNT = 1; // 通常情况下都是1
    private final long MAX_SIZE = 1024 * 1024 * 10; // 最大值,可自定义 10MB

    private DiskLruCache diskLruCache;

    public DiskLruCacheWrapper() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + DISKLRU_CACHE_DIR);
        if(!file.exists()){
            file.mkdir();
        }
        try {
            diskLruCache = DiskLruCache.open(file, APP_VERSION, VALUE_COUNT, MAX_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(String key, Resource resource) {
        DiskLruCache.Editor editor = null;
        OutputStream outputStream = null;
        try {
            editor = diskLruCache.edit(key);
            outputStream = editor.newOutputStream(0);// index 不能大于 VALUE_COUNT
            Bitmap bitmap = resource.getBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                editor.abort();
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.e(TAG, "put: editor.abort() e:" + e.getMessage());
            }
        } finally {
            try {
                editor.commit();

                diskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "put: editor.commit(); e:" + e.getMessage());
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "put: outputStream.close(); e:" + e.getMessage());
                }
            }
        }
    }

    public Resource get(String key) {
        InputStream inputStream = null;
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            // 判断快照不为null的情况下，在去读取操作
            if (null != snapshot) {
                Resource resource = Resource.getInstance();
                inputStream = snapshot.getInputStream(0);// index 不能大于 VALUE_COUNT
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                resource.setBitmap(bitmap);
                // 保存key 唯一标识
                resource.setKey(key);
                return resource;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "get: inputStream.close(); e:" + e.getMessage());
                }
            }
        }
        return null;
    }
}
