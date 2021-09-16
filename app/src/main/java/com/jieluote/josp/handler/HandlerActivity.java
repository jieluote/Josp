package com.jieluote.josp.handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.util.Printer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jieluote.josp.R;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Handler测试类,涉及到的知识点:
 * 1.模拟实现handler整个消息收发及延时过程(包含 Handlers,Messages,Loopers.MessageQueues,后缀都加了s)
 * 2.Handler收发消息时记录日志的方式(对性能优化有帮助)
 * 3.sleep对延时消息的影响
 * 4.同步消息,异步消息,及屏障消息
 * 5.idleHandler的使用
 * 6.子线程更新UI
 */
public class HandlerActivity extends AppCompatActivity {
    private TextView mShowTv;
    private TextView mtempTv;
    private static final int UPDATE_VIEW = 100;
    private Handlers mHandlers;
    private LinearLayout mContainer;
    private Button mResponseBtn;
    /*模拟handlers不能做为成员变量new
    private Handlers mHandlers = new Handlers(new Handlers.Callback() {
        @Override
        public boolean handleMessage(Messages msg) {
            if(msg.what == UPDATE_VIEW){
                mTv.setText((String)msg.obj);
            }
            return false;
        }
    });*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.handler_activity);
        mContainer = findViewById(R.id.container);
        mShowTv = findViewById(R.id.test_tv);
        mResponseBtn = findViewById(R.id.response_btn);
        mResponseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HandlerActivity.this, "有响应", Toast.LENGTH_SHORT).show();
            }
        });
        //testHandlerByThread();
        testOriginalHandler();
        //testIdleHandle();
    }

    /**
     * 原生Handler 测试主线程
     */
    private void testOriginalHandler(){
        Log.d("HandlersTest", "start testOriginalHandler");
        Handler handler = new Handler(){

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what == UPDATE_VIEW){
                    Log.d("HandlersTest", "testOriginalHandler handleMessage:" + (String)msg.obj);
                    mShowTv.setText((String)msg.obj);
                }
            }
        };
        Message message = new Message();
        message.what = UPDATE_VIEW;
        message.obj = "Original delay 2000";
        handler.sendMessageDelayed(message,2000);

        Message msg = new Message();
        msg.what = UPDATE_VIEW;
        msg.obj = "Original no delay 4000";
        handler.sendMessageDelayed(msg,4000);

        //这里休眠用来测试对Delayed的msg是否有影响
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //这里用来测试异步消息,如果不搭配同步屏障则和普通消息没有任何区别
        Message asyncMsg = new Message();
        asyncMsg.what = UPDATE_VIEW;
        asyncMsg.setAsynchronous(true);//设置异步消息
        asyncMsg.obj = "Original Async";
        handler.sendMessage(asyncMsg);

        //同步屏障(不能直接调用,需要用到反射),发送屏障消息后,普通消息屏蔽
        //只执行异步消息, 直到移除同步屏障
        //int barrier = handler.getLooper().getQueue().postSyncBarrier();
        //handler.getLooper().getQueue().removeSyncBarrier(barrier);
    }

    /**
     * 原生Handler 测试子线程
     */
    private void testThreadOriginalHandler() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.d("HandlersTest", "testThreadHandler run:" + currentThread().getName());
                Looper.prepare();
                //这里必须要传入getMainLopper,否则不能更新UI
                Handler handler = new Handler(getMainLooper()) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == UPDATE_VIEW) {
                            Log.d("HandlersTest", "testThreadHandler handleMessage:" + (String) msg.obj);
                            try {
                                mShowTv.setText((String) msg.obj);
                            } catch (Exception e) {
                                Log.d("HandlersTest", "Exception:" + e);
                            }
                        }
                    }
                };
                Message message = new Message();
                message.what = UPDATE_VIEW;
                message.obj = "delay";
                handler.sendMessage(message);


                Message msg = new Message();
                msg.what = UPDATE_VIEW;
                msg.obj = "no delay";
                handler.sendMessage(msg);
                Looper.loop();
            }
        }.start();
    }

    /***
     * 原生handler 测试IdleHandler使用
     */
    private void testIdleHandle() {
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                //虽然是IdleHandle,但是仍然是在主线程调用的,避免做耗时操作
                Log.d("HandlersTest", "run queueIdle,thread:" + Thread.currentThread().getName());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //返回值是否keep
                // false: 移除  true:下次idle时还会再次调用
                return false;
            }
        });
    }

    /**
     * 自定义Handlers  UI线程(主线程)模拟,loop的for死循环会阻塞页面
     */
    private void testHandlerByUI() {
        Loopers.prepareMainLooper();
        mHandlers = new Handlers(new Handlers.Callback() {
            @Override
            public boolean handleMessage(Messages msg) {
                Log.d("HandlersTest", "handleMessage:" + msg.obj + ",Thread:" + Thread.currentThread().getName());
                if (msg.what == UPDATE_VIEW) {
                    mShowTv.setText((String) msg.obj);
                }
                return false;
            }
        });
        Messages delayMsg = new Messages();
        delayMsg.what = UPDATE_VIEW;
        delayMsg.obj = "UI Thread Handler delayed";
        mHandlers.sendMessageDelayed(delayMsg,0);

        Messages msg = new Messages();
        msg.what = UPDATE_VIEW;
        msg.obj = "UI Thread Handler";
        mHandlers.sendMessageDelayed(msg,0);
        Loopers.loop();
    }

    /**
     * 自定义Handlers 子线程模拟
     */
    private void testHandlerByThread() {
        new Thread() {
            @Override
            public void run() {
                Loopers.prepare();
                mHandlers = new Handlers() {
                    @Override
                    public void handleMessage(Messages msg) {
                        super.handleMessage(msg);
                        if (msg.what == UPDATE_VIEW) {
                            Log.d("HandlersTest", "UPDATE_VIEW,text:" + (String) msg.obj);
                            //为了在子线程直观演示延时更新UI效果,这里在子线程创建了view,然后通过主线程添加到容器中:
                            mtempTv = new TextView(getApplicationContext());
                            mtempTv.setText((String) msg.obj);
                            mtempTv.setTextSize(20);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mContainer.removeAllViews();
                                    mContainer.addView(mtempTv);
                                }
                            });
                        }
                    }
                };
                Messages messages = new Messages();
                messages.what = UPDATE_VIEW;
                messages.obj = "child Thread Handlers one ";
                mHandlers.sendMessageDelayed(messages, 0);


                Messages messages2 = new Messages();
                messages2.what = UPDATE_VIEW;
                messages2.obj = "child Thread Handlers two";
                mHandlers.sendMessageDelayed(messages2, 1000);
                Loopers.MyLooper().setMessageLogging(new Printer() {
                    @Override
                    public void println(String s) {
                        Log.d("HandlersTest", "Logging:" + s);
                    }
                });
                Loopers.loop();
            }
        }.start();
    }

    /**
     * 用来测试在子线程更新UI（onResume方法之后）
     */
    private void testUpdateUI() {
        Button addBtn = findViewById(R.id.add_btn);
        Button createBtn = findViewById(R.id.create_btn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //子线程不能更新在主线程创建的view(View会去判断更新线程和创建线程是否一致),但是可以更新在相同子线程创建的view
                        mtempTv = new TextView(getApplicationContext());
                        mtempTv.setText("update UI by child Thread");
                        mtempTv.setTextSize(30);
                    }
                }).start();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mContainer.removeAllViews();
                    mContainer.addView(mtempTv);
                } catch (Exception e) {
                    Log.d("HandlerTest", "addview Exception:" + e);
                }
            }
        });

    }
}