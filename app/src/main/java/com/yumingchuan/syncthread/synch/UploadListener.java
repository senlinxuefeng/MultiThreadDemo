package com.yumingchuan.syncthread.synch;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yumingchuan on 2017/11/15.
 */

public class UploadListener implements Runnable{
    private CountDownLatch downLatch;
    private OnAllThreadResultListener listener;

    public UploadListener(CountDownLatch countDownLatch,OnAllThreadResultListener listener){
        this.downLatch=countDownLatch;
        this.listener=listener;
    }

    @Override
    public void run() {
        try {
            downLatch.await();
            listener.onSuccess();//顺利完成
        } catch (InterruptedException e) {
            listener.onFailed();
        }
    }
}
