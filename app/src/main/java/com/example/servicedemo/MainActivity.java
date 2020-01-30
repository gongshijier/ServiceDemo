package com.example.servicedemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


   private TextView textView ;
    private TextView showLog ;
    private  Button startService ;
    private  Button bindService ;
    private  Button unBindService ;
    private Button stopService ;
    private  Button getnumber;
    Intent serviceIntent ;
    private MyService.RandomNumBinder randomNumBinder;
    private MyService myService;
    ServiceConnection serviceConnection;
    Boolean isBindService =false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniview();
        serviceIntent = new Intent(this,MyService.class);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.startService:
                startService(serviceIntent);
                break;
            case R.id.bindService:
                bindmService();

                break;
            case R.id.unBindService:
                unbindmService();
                break;
            case R.id.stopService:
                stopService(serviceIntent);
                break;
            case R.id.getnumber:
               setObserver();
                setmText(myService.getmRandomNumber());
                break;
        }
    }

    private void setmText(int getmRandomNumber) {
        textView.setText(getmRandomNumber+"");
    }
    public void iniview(){
        textView = findViewById(R.id.textView);
        showLog = findViewById(R.id.showLog);
        startService = findViewById(R.id.startService);
        bindService = findViewById(R.id.bindService);
        unBindService = findViewById(R.id.unBindService);
        stopService = findViewById(R.id.stopService);
        getnumber = findViewById(R.id.getnumber);
        startService.setOnClickListener(this);
        bindService.setOnClickListener(this);
        unBindService.setOnClickListener(this);
        stopService.setOnClickListener(this);
        getnumber.setOnClickListener(this);
    }
    //bind service获取调动服务的接口myService可以来和Service交互
    public void bindmService(){
         serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                randomNumBinder = (MyService.RandomNumBinder) service;
                myService =  randomNumBinder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(serviceIntent,serviceConnection,BIND_AUTO_CREATE);
        isBindService = true;
    }
    public void unbindmService(){
        if(isBindService == true){
            unbindService(serviceConnection);
        }
        isBindService = false;
    }
    //根据Service内的Livedata来更新数据显示到界面
    public void setObserver(){
        myService.getMutableLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                showLog.setText(s);
            }
        });
    }

}
