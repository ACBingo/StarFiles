package com.example.acbingo.myfinaltest10.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.acbingo.myfinaltest10.DeviceManager.AllClientDevices;
import com.example.acbingo.myfinaltest10.DeviceManager.Device;
import com.example.acbingo.myfinaltest10.FileChooseService.ChooseFileActivity;
import com.example.acbingo.myfinaltest10.FileChooseService.FileChoose;
import com.example.acbingo.myfinaltest10.MyWifiManager.MyWifiManager;
import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.VoiceSystem.SendVoice;
import com.example.acbingo.myfinaltest10.VoiceSystem.VoiceContent;
import com.jaeger.library.StatusBarUtil;
import com.skyfishjy.library.RippleBackground;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by tfuty on 2016-05-24.
 */
public class SendSignalActivity extends AppCompatActivity {
    ImageView rippleimg;

    private BadgeView badgeView;
    private SendVoice sendVoice;
    private MyWifiManager myWifiManager;
    RippleBackground rippleBackground;
    Button button_next;
    Button button_choose;

    boolean serverlistensocket =true;
    boolean SEND_YES = true;
    int SendCnt = 0;

    int DeviceCnt = 0;

    String sendMsg;


    //Todo
    public void updateUI(final String s){
        Log.d("mydebug",s);
    }
    
    public void updateBadge(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                badgeView.setValue(s);
            }
        });
    }

    public void updateDevice(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //button_next.setVisibility(View.VISIBLE);
                //Todo 添加一个新图标

                DeviceCnt++;
            }
        });
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_signal);

        SEND_YES = true;
        //状态栏透明
        StatusBarUtil.setTransparent(this);

        sendVoice = new SendVoice(getBaseContext(),handler);
        myWifiManager = MyWifiManager.getMyWifiManagerInstance(getBaseContext());

        sendMsg = myWifiManager.getWifiHotSSID().substring(myWifiManager.wifiHotPrefix.length())
                +myWifiManager.getWifiHotPassword();

        rippleBackground=(RippleBackground)findViewById(R.id.ripple);
        rippleimg = (ImageView) findViewById(R.id.send_ripple_Image);
        badgeView = (BadgeView) findViewById(R.id.badge_view);
        button_next = (Button) findViewById(R.id.next);
        button_choose = (Button) findViewById(R.id.choosefile);


        button_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent te = new Intent(SendSignalActivity.this,ChooseFileActivity.class);
                startActivityForResult(te,ChooseFileActivity.CHOOSEFILEACTIVITYRESULT);
            }
        });
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FileChoose.getFileChooseCount()==0){
                    Toast.makeText(SendSignalActivity.this,"您还没有选择任何文件哦，请先选择文件",Toast.LENGTH_SHORT).show();
                }else if (DeviceCnt==0){
                    Toast.makeText(SendSignalActivity.this,"还没有任何设备连接上哦，请等待设备连接",Toast.LENGTH_SHORT).show();
                }else{
                    //Todo startActivity
                    SEND_YES = false;
                    serverlistensocket=false;
                    startActivity(new Intent(SendSignalActivity.this,SendFileInfoActivity.class));
                    finish();
                }
            }
        });

        //断开所有连接（ap、wifi、数据），同时创建一个随机的wifi热点
        //Todo



        /*rippleimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rippleBackground.startRippleAnimation();
                updateBadge("正在发送声波");
            }
        });


        final int duration = 750;
        badgeView.postDelayed(new Runnable() {
            @Override public void run() {

                new BadgeView.AnimationSet(badgeView)
                        .add("创建wifi热点中", 1000)
                        .add("正在将热点信息编码到声波中", 1200)
                        //.add("正在发送声波", 2000)
                        .play();
            }
        }, 1000);
*/
        init();
    }

    private void init(){
        //Todo 提示用户当前状态的转变

        myWifiManager.disconnectAll();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (myWifiManager.getWifiApState()!=myWifiManager.WIFI_AP_STATE_DISABLED){
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                updateBadge("初始化完毕");
                updateBadge("正在创建随机热点");

                myWifiManager.wifiHotCreate();
                while (myWifiManager.getWifiApState()!=myWifiManager.WIFI_AP_STATE_ENABLED){
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                updateBadge("热点创建成功");
                updateBadge("当前热点为:"+MyWifiManager.wifiHotPrefix+MyWifiManager.getWifiHotSSID());

                updateBadge("正在将热点信息编码到声波中");

                updateBadge("正在发送声波,请把声音调大哦^_^");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rippleBackground.startRippleAnimation();
                    }
                });

                //Todo 开启serversocket等待连接

                new Thread(new Runnable() {
                    ServerSocket ss;
                    Socket socket;
                    @Override
                    public void run() {
                        try {
                            ss = new ServerSocket(5418);
                            while (serverlistensocket){
                                //Todo 用户点了确定后break


                                socket = ss.accept();


                                //Todo 通知ui显示一个新的图标
                                updateDevice();


                                Device device = new Device(socket);
                                device.sendback();
                                AllClientDevices.clientDevices.add(device);
                                updateBadge("一台新设备已连接，当前连接总数:"+ AllClientDevices.clientDevices.size());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            try{
                                if (ss!=null) {
                                    ss.close();

                                }
                            }catch (IOException e){}
                        }
                    }
                }).start();

                SEND_YES = true;

                sendVoice.send(sendMsg);


            }
        }).start();
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case VoiceContent.MSG_PLAY_END:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (SEND_YES){
                                sendVoice.send(sendMsg);
                                SendCnt++;

                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rippleBackground.stopRippleAnimation();
                                    }
                                });

                                //Todo
                                //startActivity(new Intent(SendActivity.this,FileChooseActivity.class));
                            }

                        }
                    }).start();
                    break;
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        //SEND_YES = true;
        //init();
    }

    @Override
    public void onPause(){
        super.onPause();
        //sendVoice.stop();
        //SEND_YES = false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        sendVoice.uninit();
        SEND_YES = false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case ChooseFileActivity.CHOOSEFILEACTIVITYRESULT:
                FileChoose myfilechoos = FileChoose.getInstance();
                ArrayList<String> all = data.getStringArrayListExtra(ChooseFileActivity.ALLSELECTEDPATH);
                ArrayList<File> stc = FileChoose.getFileChooseArrayList();
                stc.clear();
                for(String x : all){
                      Log.d("zhaoshuai","--> + " + x);
                      myfilechoos.addFile(x);
                }
        }
    }
    @Override
    public void onBackPressed(){
         FileChoose.getFileChooseArrayList().clear();
         finish();
    }
}
