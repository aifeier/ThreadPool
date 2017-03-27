package com.ai.cwf.threadpool;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.ListViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //配置单选器
    private AlertDialog.Builder builder;

    private int executeLength = 10;
    private int showConfig = View.VISIBLE;
    private LinearLayout configView;

    //线程池
    private ThreadPoolExecutor mThreadPoolExecutor;

    //线程池配置
    private int corePoolSize = 2;
    private int maximumPoolSize = 10;
    private long keepAliveTime = 60L;
    private TimeUnit unit = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private ThreadFactory threadFactory;

    private ArrayList<Mold> timeUnitList;
    private ArrayList<Mold> workQueueList;

    private AppCompatButton btAliveTimeUnit, btWorkQueue;
    AppCompatEditText etCorePoolSize, etMaximumPoolSize, etKeepAliveTime, etExecuteLength;


    private ListViewCompat logView;
    private ArrayAdapter<String> mAdapter;
    private List<String> logs;

    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == 1) {
                logs.add((String) msg.obj);
                mAdapter.notifyDataSetChanged();
            } else if (msg.what == 2) {
            }
            super.dispatchMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSelectList();
        findViewById(R.id.execute).setOnClickListener(this);
        findViewById(R.id.shutdown).setOnClickListener(this);
        findViewById(R.id.shutdownNow).setOnClickListener(this);
        configView = (LinearLayout) findViewById(R.id.configView);
        etExecuteLength = (AppCompatEditText) findViewById(R.id.etExecuteLength);
        etCorePoolSize = (AppCompatEditText) findViewById(R.id.etCorePoolSize);
        etMaximumPoolSize = (AppCompatEditText) findViewById(R.id.etMaximumPoolSize);
        etKeepAliveTime = (AppCompatEditText) findViewById(R.id.etKeepAliveTime);
        btAliveTimeUnit = (AppCompatButton) findViewById(R.id.btAliveTimeUnit);
        btWorkQueue = (AppCompatButton) findViewById(R.id.btWorkQueue);
        logView = (ListViewCompat) findViewById(R.id.logView);
        logView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        btAliveTimeUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleDialog(timeUnitList, timeUnitClick);
            }
        });
        btWorkQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleDialog(workQueueList, workQueueClick);
            }
        });
        findViewById(R.id.configure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showConfig == View.VISIBLE)
                    showConfig = View.GONE;
                else showConfig = View.VISIBLE;
                configView.setVisibility(showConfig);
            }
        });
        logs = new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, logs);
        logView.setAdapter(mAdapter);
    }

    private void initSelectList() {
        timeUnitList = new ArrayList<>();
        timeUnitList.add(new Mold(TimeUnit.DAYS, "天"));
        timeUnitList.add(new Mold(TimeUnit.HOURS, "小时"));
        timeUnitList.add(new Mold(TimeUnit.MINUTES, "分钟"));
        timeUnitList.add(new Mold(TimeUnit.SECONDS, "秒"));
        timeUnitList.add(new Mold(TimeUnit.MILLISECONDS, "毫秒"));
        timeUnitList.add(new Mold(TimeUnit.MICROSECONDS, "微秒"));
        workQueueList = new ArrayList<>();
        workQueueList.add(new Mold(new SynchronousQueue<>(), "SynchronousQueue"));
        workQueueList.add(new Mold(new SynchronousQueue<>(true), "SynchronousQueue fair"));
        workQueueList.add(new Mold(new LinkedBlockingQueue<Runnable>(10), "LinkedBlockingQueue HasLimit 10"));
        workQueueList.add(new Mold(new LinkedBlockingQueue<Runnable>(), "LinkedBlockingQueue"));
        workQueueList.add(new Mold(new ArrayBlockingQueue<Runnable>(10), "ArrayBlockingQueue HasLimit 10 "));
        workQueueList.add(new Mold(new ArrayBlockingQueue<Runnable>(10, true), "ArrayBlockingQueue HasLimit 10 fail "));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.execute:
                mAdapter.notifyDataSetChanged();
                executeLength = Integer.valueOf(TextUtils.isEmpty(etExecuteLength.getText().toString()) ? "10" : etExecuteLength.getText().toString());
                corePoolSize = Integer.valueOf(TextUtils.isEmpty(etCorePoolSize.getText().toString()) ? "2" : etCorePoolSize.getText().toString());
                maximumPoolSize = Integer.valueOf(TextUtils.isEmpty(etMaximumPoolSize.getText().toString()) ? "10" : etMaximumPoolSize.getText().toString());
                keepAliveTime = Long.valueOf(TextUtils.isEmpty(etKeepAliveTime.getText().toString()) ? "60" : etKeepAliveTime.getText().toString());
                if (corePoolSize > maximumPoolSize) {
                    Toast.makeText(MainActivity.this, "总线程必须大于核心线程", Toast.LENGTH_SHORT);
                }
                try {
                    if (mThreadPoolExecutor != null && mThreadPoolExecutor.getActiveCount() > 0) {
                        Toast.makeText(MainActivity.this, "请等待上一次线程池结束或shutdownNow" + mThreadPoolExecutor.getActiveCount(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    logs.clear();
                    if (mThreadPoolExecutor == null || mThreadPoolExecutor.isShutdown()) {
                        mThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                                keepAliveTime, unit, workQueue);
                    }
                    for (int i = 1; i <= executeLength; i++) {
                        mThreadPoolExecutor.execute(new TestRunnable("进程" + i, i * 2, mHandler));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "线程启动失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.shutdown:
                if (mThreadPoolExecutor != null && !mThreadPoolExecutor.isShutdown())
                    mThreadPoolExecutor.shutdown();
                break;
            case R.id.shutdownNow:
                if (mThreadPoolExecutor != null && !mThreadPoolExecutor.isShutdown())
                    mThreadPoolExecutor.shutdownNow();
                break;
        }

    }

    private DialogInterface.OnClickListener timeUnitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            unit = (TimeUnit) timeUnitList.get(which).key;
            btAliveTimeUnit.setText(timeUnitList.get(which).value);
        }
    };

    private DialogInterface.OnClickListener workQueueClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            workQueue = (BlockingQueue<Runnable>) workQueueList.get(which).key;
            btWorkQueue.setText(workQueueList.get(which).value);
        }
    };

    void showSingleDialog(final ArrayList<Mold> list, DialogInterface.OnClickListener listener) {
        if (builder == null) {
            builder = new AlertDialog.Builder(this);
        }

        String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            items[i] = list.get(i).value;
        }

        builder.setTitle("请选择");
        builder.setItems(items, listener);
        builder.create().show();
    }
}
