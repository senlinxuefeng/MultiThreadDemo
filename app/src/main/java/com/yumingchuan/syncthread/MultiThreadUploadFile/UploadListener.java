package com.yumingchuan.syncthread.MultiThreadUploadFile;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yumingchuan on 2017/11/15.
 */

public class UploadListener implements Runnable {
    private CountDownLatch downLatch;
    private OnAllThreadResultListener listener;

    public UploadListener(CountDownLatch countDownLatch, OnAllThreadResultListener listener) {
        this.downLatch = countDownLatch;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            downLatch.await();//等待所有工作完成，完成后才会执行下面的
            listener.onSuccess();//顺利完成
        } catch (InterruptedException e) {
            listener.onFailed();
        }
    }
}
