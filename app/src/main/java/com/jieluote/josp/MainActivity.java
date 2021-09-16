package com.jieluote.josp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jieluote.josp.blockingQueue.BlockingQueueActivity;
import com.jieluote.josp.glide.activity.GlideActivity;
import com.jieluote.josp.handler.HandlerActivity;
import com.jieluote.josp.threadPool.ThreadPoolActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Button testHandlerBtn = findViewById(R.id.test_handler_btn);
        testHandlerBtn.setOnClickListener(this);
        Button testBlockingQueueBtn = findViewById(R.id.test_blocking_queue_btn);
        testBlockingQueueBtn.setOnClickListener(this);
        Button testThreadPoolBtn = findViewById(R.id.test_thread_pool_btn);
        testThreadPoolBtn.setOnClickListener(this);
        Button testGlideBtn = findViewById(R.id.test_glide_btn);
        testGlideBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_handler_btn:
                Intent intent = new Intent(this, HandlerActivity.class);
                startActivity(intent);
                break;
            case R.id.test_blocking_queue_btn:
                Intent intent2 = new Intent(this, BlockingQueueActivity.class);
                startActivity(intent2);
                break;
            case R.id.test_thread_pool_btn:
                Intent intent3 = new Intent(this, ThreadPoolActivity.class);
                startActivity(intent3);
                break;
            case R.id.test_glide_btn:
                Intent intent4 = new Intent(this, GlideActivity.class);
                startActivity(intent4);
                break;
            default:
                break;
        }
    }
}
