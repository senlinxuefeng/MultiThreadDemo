package com.yumingchuan.syncthread.MultiThreadUploadFile;

/**
 * Created by yumingchuan on 2017/11/15.
 */

public interface OnThreadResultListener {
    void onProgressChange(int percent);//进度变化回调
    void onFinish();//线程完成时回调
    void onInterrupted();//线程被中断回调
}
