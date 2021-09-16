package com.jieluote.josp.glide;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.jieluote.josp.glide.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;

public class EngineJob implements DecodeJob.Callback{
    private final String TAG = Constant.TAG;
    private DecodeJob decodeJob;
    private static volatile int bestThreadCount;
    private static final int MAXIMUM_AUTOMATIC_THREAD_COUNT = 4;
    private final List<Engine.ResourceCallback> cbs = new ArrayList<>(2);

    private static final int MSG_COMPLETE = 1;
    private static final int MSG_EXCEPTION = 2;

    //将从子线程的返回的数据切换到主线程供上层回调
    private final Handler MAIN_THREAD_HANDLER =
            new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MSG_COMPLETE:
                            if (cbs == null) {
                                return;
                            }
                            for (Engine.ResourceCallback c : cbs) {
                                c.onResourceReady((Resource) msg.obj);
                            }
                            break;
                        case MSG_EXCEPTION:
                            if (cbs == null) {
                                return;
                            }
                            for (Engine.ResourceCallback c : cbs) {
                                c.onLoadFailed((Exception) msg.obj);
                            }
                            break;
                    }
                }
            };

    public void start(DecodeJob decodeJob) {
        this.decodeJob = decodeJob;
        if (decodeJob != null) {
            decodeJob.setResourceCallback(this);
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                calculateBestThreadCount() /* corePoolSize */,
                calculateBestThreadCount() /* maximumPoolSize */,
                0 /* keepAliveTime */,
                TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>());
        Log.d(TAG, "EngineJob start, execute decodeJob");
        //开启了线程池执行解码(获取数据)任务
        executor.execute(decodeJob);
    }

    public static int calculateBestThreadCount() {
        if (bestThreadCount == 0) {
            bestThreadCount =
                    Math.min(MAXIMUM_AUTOMATIC_THREAD_COUNT, Utils.availableProcessors());
        }
        return bestThreadCount;
    }

    public void addCallback(Engine.ResourceCallback resourceCallback) {
        if(resourceCallback != null){
            cbs.add(resourceCallback);
        }
    }

    public void removeCallback(Engine.ResourceCallback resourceCallback) {
        if(resourceCallback != null){
            cbs.remove(resourceCallback);
        }
    }

    @Override
    public void onResourceReady(Resource resource) {
        Message message = MAIN_THREAD_HANDLER.obtainMessage();
        message.what = MSG_COMPLETE;
        message.obj = resource;
        MAIN_THREAD_HANDLER.sendMessage(message);
    }

    @Override
    public void onLoadFailed(Exception e) {
        Message message = MAIN_THREAD_HANDLER.obtainMessage();
        message.what = MSG_EXCEPTION;
        message.obj = e;
        MAIN_THREAD_HANDLER.sendMessage(message);
    }
}
