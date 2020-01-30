# ServiceDemo
Android：探究后台：服务 BindService Binder LiveData
# 服务Service  
服务有两种启动方式：
* startService（）方式开始的服务  
* bindService（）方式开启的服务  
***    
## LivaData(Demo中使用到)依赖：
```
    implementation "android.arch.lifecycle:extensions:1.1.1"
    implementation "android.arch.lifecycle:viewmodel:1.1.1"
    implementation "android.arch.lifecycle:livedata:1.1.1"
```
***    
## 启动和特性  
服务仍然是运行在主线程，两种服务有着不同的生命周期，也可以搭配一起使用。  
两种方法都可以实现数据从活动到服务，通过在intent添加数据，可以在服务内从intent内取出数据。
```
//在Intent内添加数据就可以在服务内从Intent取出
 startService(serviceIntent); 
 bindService(serviceIntent,serviceConnection,BIND_AUTO_CREATE);
```   
在服务内获取来自intent的数据   
```
    //Bind方式启动服务时候调用，返回IBinder类型的接口供活动调用
    @Override
    public IBinder onBind(Intent intent) {
     // intent即可取出携带数据
        Log.i("gong","sevice bind");
        return randomNumBinder;
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
```
***  
## start方式启动的服务  
该方式的特点是和活动之间没有很强的交互性，可以利用LiveData增加数据交互性。  
该种方式会调用onStartConmmand（），在该方法内我们实现需要进行的后台服务操作逻辑。  
该函数的返回值的类型有三种，适用于不同类型的需求。  
![官方文档中的介绍](https://upload-images.jianshu.io/upload_images/19741117-37cb3be555afc2ab.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
如果在服务中开启了线程，注意在服务Desdroy后，线程不会自动终止，需要自己实现**在服务销毁后终止线程**。线程会在执行run后终止。  
***  
## bind方式启动的服务  
该方式通过Binder机制，可以实现活动和服务的交互，这种交互很强，包括了**数据上的交互和对服务行为上的控制**。完全可以通过Binder获得Service实例从而控制调用Service内的方法。  
![bindService的几种类型和实现](https://upload-images.jianshu.io/upload_images/19741117-28a6aa0637b5499a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
* Local  binding指在应用进程内进行bind  
* Remote binding指跨进程实现的bind  
注意跨进程通信即（IPC）涉及的重要概念：  
* IBinder接口是用来交互的桥梁  
* Messenger继承自Binder也是实现IPC的一种方法  
* AIDL也是用于实现IPC  
 ![local binding](https://upload-images.jianshu.io/upload_images/19741117-4303b21bcd218b7c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
![remote binding](https://upload-images.jianshu.io/upload_images/19741117-ba685e3ff0d2e9cb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
```

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
```  

绑定端代码  
```
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
```
***  
## 服务service生命周期  
![生命周期](https://upload-images.jianshu.io/upload_images/19741117-371b8459655cc326.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
***  
## 总结  
##### 需求选择启动方式  
根据具体需要来启动Service，建立service后，可以理解为客户端和服务端。  
* 如果只是需要进行数据上的交互，建议start方式+ LiveData 即可保证service内的数据通过Livedata可以在活动中获取。  
* 如果还需要对服务进行行为控制，强交互，比如控制服务内的函数的调用，那么就需要我使用bindService方式启动服务，还可以搭配start方式后bind根据具体需求。  
##### 生命周期的问题：
* 结束服务：其他组件调用stopService（）或者 服务内自行调用stopself（）  
* bind的service只有unbind后才可以调用stopservice来结束  
* 可以开启多个服务，调用一次stopService即可结束所有服务  
* bind也可以直接开启服务当所有bind的组件unbind后service就会销毁  
* 如果需要启动多个service可以使用官方文档推荐的方法使用Handler机制来处理  
```
public class HelloService extends Service {
  private Looper mServiceLooper;
  private ServiceHandler mServiceHandler;

  // Handler that receives messages from the thread
  private final class ServiceHandler extends Handler {
      public ServiceHandler(Looper looper) {
          super(looper);
      }
      @Override
      public void handleMessage(Message msg) {
          // Normally we would do some work here, like download a file.
          // For our sample, we just sleep for 5 seconds.
          long endTime = System.currentTimeMillis() + 5*1000;
          while (System.currentTimeMillis() < endTime) {
              synchronized (this) {
                  try {
                      wait(endTime - System.currentTimeMillis());
                  } catch (Exception e) {
                  }
              }
          }
          // Stop the service using the startId, so that we don't stop
          // the service in the middle of handling another job
          stopSelf(msg.arg1);
      }
  }

  @Override
  public void onCreate() {
    // Start up the thread running the service.  Note that we create a
    // separate thread because the service normally runs in the process's
    // main thread, which we don't want to block.  We also make it
    // background priority so CPU-intensive work will not disrupt our UI.
    HandlerThread thread = new HandlerThread("ServiceStartArguments",
            Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();

    // Get the HandlerThread's Looper and use it for our Handler
    mServiceLooper = thread.getLooper();
    mServiceHandler = new ServiceHandler(mServiceLooper);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
      Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

      // For each start request, send a message to start a job and deliver the
      // start ID so we know which request we're stopping when we finish the job
      Message msg = mServiceHandler.obtainMessage();
      msg.arg1 = startId;
      mServiceHandler.sendMessage(msg);

      // If we get killed, after returning from here, restart
      return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
      // We don't provide binding, so return null
      return null;
  }

  @Override
  public void onDestroy() {
    Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
  }
}
```
使用Handler机制细思极妙，官方文档需要经常查看。  
***  
## MyServiceDemo代码  
MyService.java  
```
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
```
MainActivity.java  
```

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
```





