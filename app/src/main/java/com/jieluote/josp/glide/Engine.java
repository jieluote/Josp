package com.jieluote.josp.glide;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import com.jieluote.josp.glide.load.ActiveCache;
import com.jieluote.josp.glide.load.DiskLruCacheWrapper;
import com.jieluote.josp.glide.load.MemoryCache;
import com.jieluote.josp.glide.load.request.RequestListener;

/***
 * 引擎,负责数据流的加载转换与保存
 */
public class Engine implements ResourceListener{
    private final String TAG = Constant.TAG;
    private Key key;
    private KeyFactory keyFactory;
    private Object model;
    private Object target;
    private RequestListener targetListener;
    private Context context;
    private ActiveCache activeCache; // 活动缓存
    private MemoryCache memoryCache; // 内存缓存
    private DiskLruCacheWrapper diskLruCache; // 磁盘缓存
    private EngineJob engineJob;
    private DecodeJob decodeJob;

    public Engine(ActiveCache activeCache, MemoryCache memoryCache, DiskLruCacheWrapper diskLruCache) {
        this.activeCache = activeCache;
        this.activeCache.setResourceListener(this);
        this.memoryCache = memoryCache;
        this.diskLruCache = diskLruCache;
        this.keyFactory = new KeyFactory();
    }

    public ActiveCache getActiveCache() {
        return activeCache;
    }

    public void setActiveCache(ActiveCache activeCache) {
        this.activeCache = activeCache;
    }

    public MemoryCache getMemoryCache() {
        return memoryCache;
    }

    public void setMemoryCache(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
    }

    public DiskLruCacheWrapper getDiskLruCache() {
        return diskLruCache;
    }

    public void setDiskLruCache(DiskLruCacheWrapper diskLruCache) {
        this.diskLruCache = diskLruCache;
    }

    public void load(Object model, Object target, RequestListener targetListener, GlideContext context){
        Log.d(TAG,"Engine start load");
        this.model = model;
        this.target = target;
        this.targetListener = targetListener;

        //构造key,源码中根据多个属性(path,width,height,options等转成key),这里只针对了path构造
        this.key = keyFactory.buildKey((String)model);
        final String keyValue = key.getKeyValue();

        //第一步,从活动缓存中查找key对应的resource
        Resource resource = activeCache.get(keyValue);
        if (null != resource) {
            Log.d(TAG, "load img from activityResources");
            ((ImageView) target).setImageBitmap(resource.getBitmap());
            onResourceReady(resource, model, target);
            resource.acquire(); //引用计数+1
            return;
        }

        //第二步,从内存缓存中去找,如果找到了,内存缓存中的元素移动到活动缓存中
        resource = memoryCache.get(keyValue);
        if (null != resource) {
            Log.d(TAG, "load img from memoryCache");
            memoryCache.remove(keyValue);        // 移除内存缓存
            activeCache.put(keyValue, resource); // 放入到活动缓存
            ((ImageView) target).setImageBitmap(resource.getBitmap());
            onResourceReady(resource, model, target);
            resource.acquire(); //引用计数+1
            return;
        }

        //第三步,从磁盘缓存中去找,如果找到了,把磁盘缓存中的元素加入到活动缓存中
        resource = diskLruCache.get(keyValue);
        if (null != resource) {
            Log.d(TAG, "load img from diskLruCache");
            activeCache.put(keyValue, resource);
            ((ImageView) target).setImageBitmap(resource.getBitmap());
            onResourceReady(resource, model, target);
            resource.acquire(); // 引用计数+1
            return;
        }

        //构建 engineJob(线程池) 和 decodeJob(解码 runnable) 重要!
        //源码中engineBob和 DecodeJob是使用 对象池Pools创建的,这里我们直接new
        if(engineJob == null){
            engineJob = new EngineJob();
        }
        if(decodeJob == null){
            decodeJob = new DecodeJob((String)model, context);
        }
        engineJob.addCallback(new ResourceCallback());
        engineJob.start(decodeJob); //启动线程池
    };

    public void release(Resource resource) {
        Log.d(TAG, "Engine release");
        resource.release();
    }

    @Override
    public void onResourceReleased(String key, Resource resource) {
        //引用数为0,清空活动缓存并放入内存缓存
        Log.d(TAG, "onResourceReleased,key:" + key + ",resource:" + resource.hashCode());
        activeCache.remove(key);
        memoryCache.put(key, resource);
    }

    class ResourceCallback implements DecodeJob.Callback{

        @Override
        public void onResourceReady(Resource resource) {
            ((ImageView) target).setImageBitmap(resource.getBitmap());
            Engine.this.onResourceReady(resource, model, target);
            //从网络加载成功后的原始数据放入磁盘缓存中
            if (diskLruCache != null) {
                resource.setKey(key.getKeyValue());
                diskLruCache.put(resource.getKey(), resource);
            }
            engineJob.removeCallback(this);
            //接着进行转码的操作(圆角,裁切,灰度等),处理后的数据依然会放入缓存中,这里就不再模拟了
        }

        @Override
        public void onLoadFailed(Exception e) {
            if(targetListener != null){
                targetListener.onLoadFailed(e, model, target);
            }
            engineJob.removeCallback(this);
        }
    }

    private void onResourceReady(Resource resource, Object model, Object target) {
        if (targetListener != null) {
            targetListener.onResourceReady(resource, model, target);
        }
    }
}
