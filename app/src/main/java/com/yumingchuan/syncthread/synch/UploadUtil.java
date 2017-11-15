package com.yumingchuan.syncthread.synch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yumingchuan on 2017/11/15.
 */

public class UploadUtil {
    private final static int THREAD_PROGRESS_CODE=100;//线程进度回调
    private final static int THREAD_FINISH_CODE=101;//线程完成
    private final static int THREAD_INTERRUPT_CODE=102;//线程被中断
    private final static int THREAD_ALL_SUCCESS_CODE=103;//所有线程完成
    private final static int THREAD_ALL_FAILED_CODE=104;//所有线程执行失败
    private final static String THREAD_PERCENT="THREAD_PERCENT";
    private final static String THREAD_POSITION="THREAD_POSITION";

    private int threadCount=0;//任务数量
    private int threadCore=2;//线程池核心数

    private ExecutorService executor;//线程池
    private CountDownLatch downLatch;//计数器

    private OnUploadListener uploadListener;
    private UploadHandler handler;

    public UploadUtil(){
        init();
    }

    public UploadUtil(int threadCore){
        this.threadCore=threadCore;
        init();
    }

    public void setOnUploadListener(OnUploadListener uploadListener){
        this.uploadListener=uploadListener;
    }

    public void init(){
        handler=new UploadHandler(this);
    }

    public void shutDownNow(){
        executor.shutdownNow();//中断所有线程的执行
    }

    public void submitAll(ArrayList<String> fileName){
        threadCount=fileName.size();
        downLatch=new CountDownLatch(threadCount);
        executor = Executors.newFixedThreadPool(threadCore+1);

        executor.submit(new UploadListener(downLatch, new OnAllThreadResultListener() {//创建一个监听线程
            @Override
            public void onSuccess() {
                handler.sendEmptyMessage(THREAD_ALL_SUCCESS_CODE);
            }

            @Override
            public void onFailed() {
                handler.sendEmptyMessage(THREAD_ALL_FAILED_CODE);
            }
        }));

        for(int i=0;i<threadCount;i++){//模拟生成任务线程
            final Bundle bundle=new Bundle();
            bundle.putInt(THREAD_POSITION,i);
            executor.submit(new UploadFile(downLatch,fileName.get(i),new OnThreadResultListener(){

                @Override
                public void onProgressChange(final int percent) {
                    bundle.putInt(THREAD_PERCENT,percent);
                    Message.obtain(handler,THREAD_PROGRESS_CODE,bundle).sendToTarget();
                }

                @Override
                public void onFinish() {
                    Message.obtain(handler,THREAD_FINISH_CODE,bundle).sendToTarget();
                }

                @Override
                public void onInterrupted() {
                    Message.obtain(handler,THREAD_INTERRUPT_CODE,bundle).sendToTarget();
                }
            }));
        }
        executor.shutdown();//关闭线程池
    }

    private static class UploadHandler extends Handler {//静态内部类+弱引用防止内存泄漏
        private WeakReference<UploadUtil> weakReference;

        private UploadHandler(UploadUtil object){
            super(Looper.getMainLooper());//执行在UI线程
            weakReference=new WeakReference<>(object);
        }

        @Override
        public void handleMessage(Message msg){
            UploadUtil uploadUtil=weakReference.get();
            if(uploadUtil!=null){
                Bundle data= (Bundle) msg.obj;
                int position;
                int percent;

                switch (msg.what){
                    case THREAD_PROGRESS_CODE:
                        position=data.getInt(THREAD_POSITION);
                        percent=data.getInt(THREAD_PERCENT);
                        uploadUtil.uploadListener.onThreadProgressChange(position,percent);
                        break;
                    case THREAD_FINISH_CODE:
                        position=data.getInt(THREAD_POSITION);
                        uploadUtil.uploadListener.onThreadFinish(position);
                        break;
                    case THREAD_INTERRUPT_CODE:
                        position=data.getInt(THREAD_POSITION);
                        uploadUtil.uploadListener.onThreadInterrupted(position);
                        break;
                    case THREAD_ALL_SUCCESS_CODE:
                        uploadUtil.uploadListener.onAllSuccess();
                        break;
                    case THREAD_ALL_FAILED_CODE:
                        uploadUtil.uploadListener.onAllFailed();
                        break;
                }
            }
        }
    }
}
