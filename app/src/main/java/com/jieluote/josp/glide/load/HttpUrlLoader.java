package com.jieluote.josp.glide.load;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.jieluote.josp.glide.BitmapPool;
import com.jieluote.josp.glide.Constant;
import com.jieluote.josp.glide.IModelLoader;
import com.jieluote.josp.glide.OnResponseListener;
import com.jieluote.josp.glide.Resource;
import com.jieluote.josp.glide.util.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * 使用httpURLConnection进行IO流的获取
 */
public class HttpUrlLoader implements IModelLoader {
    private final String TAG = Constant.TAG;
    private BitmapPool bitmapPool;

    public HttpUrlLoader(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource loadData(String path, OnResponseListener onResponseListener) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL(path);
            URLConnection urlConnection = url.openConnection();
            httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setConnectTimeout(30000);
            final int responseCode = httpURLConnection.getResponseCode();
            if (HttpURLConnection.HTTP_OK == responseCode) {
                inputStream = httpURLConnection.getInputStream();
                byte[] data = Utils.toByteArray(inputStream);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true; // 只解析边缘信息
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
                int w = options.outWidth;
                int h = options.outHeight;
                //拿到复用池中的数据
                Bitmap bitmapPoolResult = bitmapPool.get(w, h, Bitmap.Config.RGB_565);
                if (bitmapPoolResult == null) {
                    Log.d(TAG, "load bitmap from network");
                } else {
                    Log.d(TAG, "load bitmap from bitmapPool");
                }
                options.inBitmap = bitmapPoolResult; // 把复用池的Bitmap 给 inBitmap,如果为null,则不会复用
                options.inPreferredConfig = Bitmap.Config.RGB_565; // 2个字节
                options.inJustDecodeBounds = false;
                options.inMutable = true; //可被inBitmap复用
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                // 添加到复用池
                Log.d(TAG, "add to bitmapPool,bitmap:" + bitmap);
                bitmapPool.put(bitmap);
                Resource resource = Resource.getInstance();
                resource.setBitmap(bitmap);
                onResponseListener.responseSuccess(resource);
            } else {
                onResponseListener.responseFailed(new IllegalStateException("loadData connection error,code:" + responseCode));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    public boolean handles(Object model) {
        Uri uri = Uri.parse((String) model);
        if ("HTTP".equalsIgnoreCase(uri.getScheme()) || "HTTPS".equalsIgnoreCase(uri.getScheme())) {
            return true;
        }
        return false;
    }
}
