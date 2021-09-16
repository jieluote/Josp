package com.jieluote.josp.glide.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.jieluote.josp.R;
import com.jieluote.josp.glide.Constant;
import com.jieluote.josp.glide.Glide;
import com.jieluote.josp.glide.Resource;
import com.jieluote.josp.glide.load.request.RequestListener;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 *手写Glide
 * 目的:尽量以最小系统最精简的代码模拟Glide的源码。
 * 如果过于精简,那么无法详细了解Glide的运行流程和设计精髓
 * 如果过于详细,那么无疑增加学习门槛,很难对整体框架有个清晰的认识
 * 目前本demo基本取折中点,模拟了几个重要的功能逻辑同时尽量保持代码精简
 * 通过logcat可以直观看出整个流程的运行情况
 *
 * 已实现的API:
 * 1.Glide的三步走 with(),load(),into()
 * 2.加载的监听回调 RequestListener
 *
 * 已实现的功能/逻辑
 * 整体加载逻辑与源码基本一致(除转码(拉伸、裁切等功能)未实现外,基本都已实现)
 * 具体如下:
 * 1.加载过程和生命周期(Activity、Fragment)绑定
 * 2.资源的三级缓存:活动缓存--》内存缓存--》磁盘缓存
 * 3.可伸缩的弹性实现:通过注册处理器实现可替换的网络资源加载
 * 4.相关任务放到线程池中进行,任务完成后,切到主线程后回调
 * 5.BitmapPool缓存池的实现
 *
 *
 *大概的运行流程:
 with():
 Glide - Glide init --> 创建fragment -->  RequestManager(监听生命周期)--> RequestTracker(集中控制请求)

 load():
 RequestManager --> RequestBuilder --> 传入url

 into():
 RequestBuilder --> buildRequest --> requestTracker.runRequest --> request.begin()
 --> engine.load --> 三级缓存 --> engineJob(线程池) --> decodeJob(解码runnable) -->
 getModelLoaders() --> HttpUrlLoader.loadData() --> Transformation(转码) --> 回调
 */
public class GlideActivity extends AppCompatActivity {
    public final String TAG = Constant.TAG;
    final String IMG_URL = "http://lvsis.cn/otaku_house_sample.png";

    private static final int REQUEST_CODE = 100;
    private static String[] REQUEST_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.glide_activity);
        Button loadImgBtn = findViewById(R.id.load_img_btn);
        ImageView testIv = findViewById(R.id.test_img_iv);
        loadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(GlideActivity.this).load(IMG_URL).into(testIv, new RequestListener() {
                    @Override
                    public boolean onLoadFailed(@Nullable Exception e, Object model, Object target) {
                        Log.d(TAG, "GlideActivity onLoadFailed,Exception:" + e + ",model:" + model);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Resource resource, Object model, Object target) {
                        Log.d(TAG, "GlideActivity onResourceReady,model:" + model + ",thread:" + Thread.currentThread().getName());
                        return false;
                    }
                });
            }
        });
        //涉及到磁盘缓存,所以需要申请读写权限,否则缓存失效
        requestPermion();
    }

    public void requestPermion() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, REQUEST_CODE);
                } else {
                    Log.d(TAG, "permissions granted");
                }
            } else {
                Log.d(TAG, "permissions granted");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "requestPermion:" + e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult code:" + requestCode);
        // SDK权限处理结果
        if (requestCode == REQUEST_CODE) {
            // 申请权限被拒绝，则退出程序。
            if (grantResults == null || grantResults.length == 0) {
                Log.d(TAG, "permissions denied");
                Toast.makeText(this, "permissions denied", Toast.LENGTH_SHORT).show();
                requestPermion();
                return;
            }
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permissions denied");
                Toast.makeText(this, "permissions denied", Toast.LENGTH_SHORT).show();
                requestPermion();
            } else {
                Log.d(TAG, "permissions granted");
            }
        }
    }

}
