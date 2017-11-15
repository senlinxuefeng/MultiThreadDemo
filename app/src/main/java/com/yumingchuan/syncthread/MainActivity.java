package com.yumingchuan.syncthread;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yumingchuan.syncthread.synch.OnUploadListener;
import com.yumingchuan.syncthread.synch.UploadUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button mBtn;
    private TextView mThreadOne;
    private TextView mThreadTwo;
    private TextView mThreadThree;
    private TextView mThreadFour;
    private TextView mThreadFive;
    private TextView mState;
    private SparseArray<TextView> mTextArray;

    private volatile boolean isRunning=false;//判断线程池是否运行 标志位

    private UploadUtil mUploadUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();

    }

    private void initView() {
        mBtn= (Button) findViewById(R.id.Button);
        mThreadOne= (TextView) findViewById(R.id.ThreadOne);
        mThreadTwo= (TextView) findViewById(R.id.ThreadTwo);
        mThreadThree= (TextView) findViewById(R.id.ThreadThree);
        mThreadFour= (TextView) findViewById(R.id.ThreadFour);
        mThreadFive= (TextView) findViewById(R.id.ThreadFive);
        mState= (TextView) findViewById(R.id.ThreadState);
    }

    private void initData() {
        mTextArray=new SparseArray<>();
        mTextArray.put(0,mThreadOne);
        mTextArray.put(1,mThreadTwo);
        mTextArray.put(2,mThreadThree);
        mTextArray.put(3,mThreadFour);
        mTextArray.put(4,mThreadFive);

        mUploadUtil=new UploadUtil();
    }

    private void initListener() {
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRunning){
                    startUpload();//开始上传
                    mBtn.setText("中止上传");
                }else{
                    mUploadUtil.shutDownNow();//中断所有线程的执行
                    mBtn.setText("开始上传文件");
                }
            }
        });
        mUploadUtil.setOnUploadListener(new OnUploadListener() {//结果回调 执行在UI线程
            @Override
            public void onAllSuccess() {
                isRunning=false;
                mState.setText("全部上传成功");
            }

            @Override
            public void onAllFailed() {
                isRunning=false;
                mState.setText("上传过程中断");
            }

            @Override
            public void onThreadProgressChange(int position, int percent) {
                mTextArray.get(position).setText("文件"+position+"上传"+percent+"%");
            }

            @Override
            public void onThreadFinish(int position) {
                mTextArray.get(position).setText("文件"+position+"上传成功");
            }

            @Override
            public void onThreadInterrupted(int position) {
                mTextArray.get(position).setText("文件"+position+"上传失败");
            }
        });
    }

    private void startUpload(){//模拟上传文件
        isRunning=true;
        ArrayList<String> files=new ArrayList<>(Arrays.asList("文件一","文件二","文件三","文件四","文件五"));
        mState.setText("正在上传中......");
        mUploadUtil.submitAll(files);
    }


}
