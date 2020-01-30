package com.example.servicedemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.Random;

public class MyService extends Service {
    private static final int MAX = 100;
    private static final int MIN = 0;


    private MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
    private String logStr = new String();
    private  int mRandomNumber=0;
    private Boolean mIsRandomGeneratorOn = true;
    private IBinder randomNumBinder = new RandomNumBinder();

    //Bind方式启动服务时候调用，返回IBinder类型的接口供活动调用
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("gong","sevice bind");
        return randomNumBinder;
    }

    //构造返回的接口传递Service实例
    class RandomNumBinder extends Binder{
        MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("gong","sevice unbind!");
        return super.onUnbind(intent);
    }

    //start方式开始服务时候会调用，如果处理耗时任务需要new线程
    //startID每一次开启一个服务，都会有不同的startID
    //在开启服务时候可以在intent填写数据实现交互
    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("gong","startID: "+startId);
                startRandomNumberGenerator();
            }
        }).start();

        return START_STICKY;
    }
    private void startRandomNumberGenerator(){
        while (mIsRandomGeneratorOn){
            try{
                Thread.sleep(1000);
                if(mIsRandomGeneratorOn){
                    mRandomNumber =new Random().nextInt(MAX)+MIN;
                    logStr = logStr + mRandomNumber+"   ";
                    mutableLiveData.postValue(logStr);
                    Log.i("gong",""+mRandomNumber);
                }
            }catch (InterruptedException e){
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("gong","Service create!");
        logStr = "";
        mIsRandomGeneratorOn = true;


    }

    //销毁服务的时候记住关闭线程，线程在执行run内方法后终止
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("gong","Service destroy!");
        mIsRandomGeneratorOn = false;

    }

    public int getmRandomNumber() {
        return mRandomNumber;
    }



    //提供livadata来更新数据到活动界面
    public MutableLiveData<String> getMutableLiveData() {
        return mutableLiveData;
    }
}
