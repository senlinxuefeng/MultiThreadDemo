package com.yumingchuan.syncthread.MultiThreadUploadFile;

/**
 * Created by yumingchuan on 2017/11/15.
 */

public interface OnAllThreadResultListener {
    void onSuccess();//所有线程执行完毕
    void onFailed();//所有线程执行出现问题
}
