package com.jieluote.josp.glide.load;

import android.util.Log;
import com.jieluote.josp.glide.Constant;
import com.jieluote.josp.glide.Resource;
import com.jieluote.josp.glide.ResourceListener;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 活动缓存 (源码中是 ActiveResources,实际上也是属于内存缓存的范畴)
 * 1.存入活动缓存中的资源会关联ReferenceQueue,在while循环中一直监听其使用情况,如果没有强引用那么从活动缓存中移除
 * 2.每个资源会有引用计数器,当计数器为0时,也会从活动缓存中移除
 *
 * 为什么有了内存缓存还要活动缓存？
 * 因为活动缓存保证了当资源要使用时不会被"误伤",因为内存缓存是基于LRU的,有可能正要显示的资源正好
 * 符合LRU回收的条件,这样就只能去磁盘加载了
 */
public class ActiveCache{
    private final String TAG = Constant.TAG;
    // 容器
    private Map<String, ResourceWeakReference> resourcesMap = new HashMap<>();
    private ReferenceQueue<Resource> referenceQueue;  //引用队列,可监控对象是否被回收
    private boolean isCloseThread; //是否关闭线程,一般是Glide shutdown的时候会关闭
    private Thread cleanReferenceQueueThread;
    private ResourceListener resourceListener;

    public ActiveCache() {
    }

    public void setResourceListener(ResourceListener resourceListener) {
        this.resourceListener = resourceListener;
    }

    /**
     * 获取活动缓存
     * @param key
     * @return
     */
    public Resource get(String key) {
        WeakReference<Resource> valueWeakReference = resourcesMap.get(key);
        if (null != valueWeakReference) {
            Resource resource = valueWeakReference.get();
            //每个资源都设置引用数监听
            resource.setListener(resourceListener);
            return resource;
        }
        return null;
    }

    /**
     * 添加活动缓存
     * @param key
     * @param resource
     */
    public void put(String key, Resource resource) {
        if(key == null){
            return;
        };
        //将每一个resource和referenceQueue绑定到一起,以便能监听resource的回收情况
        resourcesMap.put(key, new ResourceWeakReference(resource, getReferenceQueue(), key));
    }

    /**
     * 移除活动缓存
     * @param key
     * @return
     */
    public void remove(String key) {
        Log.d(TAG, "remove activeCache,key:" + key);
        ResourceWeakReference remove = resourcesMap.remove(key);
        if (remove != null) {
            remove.reset();
        }
    }

    /**
     * 继承弱引用,新增属性(这里我们主要用到key)
     */
    public class ResourceWeakReference extends WeakReference<Resource> {
        private String key;
        private Resource resource;

        public ResourceWeakReference(Resource resource, ReferenceQueue<? super Resource> queue, String key) {
            super(resource, queue);
            this.resource = resource;
            this.key = key;
        }

        void reset() {
            resource = null;
            clear();
        }
    }

    /**
     * 获取ReferenceQueue
     * @return
     */
    private ReferenceQueue<Resource> getReferenceQueue() {
        if (referenceQueue == null) {
            referenceQueue = new ReferenceQueue<>();

            //监听resource弱引用是否被回收了,如果已被回收(说明resource已没有地方使用),那么就从map中移除
            cleanReferenceQueueThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (!isCloseThread) {
                        try {
                            Log.d(TAG, "ReferenceQueue start listen");
                            ResourceWeakReference weakReference = (ResourceWeakReference) referenceQueue.remove(); //如果已经被回收,会进入到Queue中(如果Queue为空,则阻塞)
                            Log.d(TAG, "ReferenceQueue remove:" + weakReference);
                            if (resourcesMap != null && !resourcesMap.isEmpty()) {
                                resourcesMap.remove(weakReference.key);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            cleanReferenceQueueThread.start();
        }
        return referenceQueue;
    }
}
