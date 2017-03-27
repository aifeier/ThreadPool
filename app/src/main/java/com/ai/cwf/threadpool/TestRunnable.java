package com.ai.cwf.threadpool;

import android.os.Handler;
import android.os.Message;

/**
 * Created at 陈 on 2017/3/27.
 *
 * @author chenwanfeng
 * @email 237142681@qq.com
 */

public class TestRunnable implements Runnable {
    private String name;
    private int stayTime = 1;
    private Handler handler;

    /*
    * @param runnableName  进程的名称
    * @param statTime 进程执行的时间 秒为单位，每2秒输出一次信息
    * */
    public TestRunnable(String runnableName, int stayTime, Handler handler) {
        this.name = runnableName;
        this.stayTime = stayTime / 2;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            for (; stayTime > 0; stayTime--) {
                if (handler != null) {
                    Message message = new Message();
                    message.what = 1;
                    message.obj = name + "：" + Thread.currentThread().getName() + "需要" + stayTime * 2 + "秒";
                    handler.sendMessage(message);
                }
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
