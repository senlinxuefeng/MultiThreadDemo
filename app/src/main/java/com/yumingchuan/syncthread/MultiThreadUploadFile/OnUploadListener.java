package com.yumingchuan.syncthread.MultiThreadUploadFile;

/**
 * Created by yumingchuan on 2017/11/15.
 */

public interface OnUploadListener {
    void onAllSuccess();
    void onAllFailed();
    void onThreadProgressChange(int position,int percent);
    void onThreadFinish(int position);
    void onThreadInterrupted(int position);
}
