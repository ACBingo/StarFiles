package com.example.acbingo.myfinaltest10.activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.example.acbingo.myfinaltest10.DeviceManager.Device;
import com.example.acbingo.myfinaltest10.DeviceManager.TheClientDevice;
import com.example.acbingo.myfinaltest10.MyWifiManager.MyWifiManager;
import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.VoiceSystem.ReceiveVoice;
import com.example.acbingo.myfinaltest10.VoiceSystem.VoiceContent;
import com.jaeger.library.StatusBarUtil;
import com.skyfishjy.library.RippleBackground;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by tfuty on 2016-05-26.
 */
public class ReceiveSignalActivity extends AppCompatActivity {
    private ReceiveVoice receiveVoice;
    private RippleBackground rippleBackground;
    private MyWifiManager myWifiManager;
    private BadgeView badgeView;
    private ImageView rippleimg;

    public void updateUI(final String s){
        Log.d("mydebug",s);
    }

    public void updateBadge(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                badgeView.setValue(s);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_signal);

        //状态栏透明
        StatusBarUtil.setTransparent(this);

        receiveVoice = new ReceiveVoice(getBaseContext(),handler);
        rippleimg = (ImageView) findViewById(R.id.send_ripple_Image);
        badgeView = (BadgeView) findViewById(R.id.badge_view);
        myWifiManager = MyWifiManager.getMyWifiManagerInstance(getBaseContext());
        rippleBackground = (RippleBackground)findViewById(R.id.ripple);

        badgeView.postDelayed(new Runnable() {
            @Override public void run() {

                new BadgeView.AnimationSet(badgeView)
                        .add("初始化成功", 1000)
                        .add("正在收听广播", 1200)
                        //.add("正在发送声波", 2000)
                        .play();
            }
        }, 1000);

        init();
    }

    private void init(){

        rippleBackground.startRippleAnimation();
        myWifiManager.disconnectAll();


        receiveVoice.start();
    }



    private char mRecgs[] = new char[100];
    private int mRecgCount;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case VoiceContent.MSG_RECG_START:
                    updateBadge("正在解码");

                    //重置缓存
                    mRecgCount = 0;
                    break;
                case VoiceContent.MSG_SET_RECG_TEXT:
                    //将一个个解码出来的字符存到缓存数组中
                    char ch = (char) msg.arg1;
                    mRecgs[mRecgCount++] = ch;
                    break;
                case VoiceContent.MSG_RECG_END:

                    if (mRecgCount > 0){
                        byte[] strs = new byte[mRecgCount];
                        for ( int i = 0; i < mRecgCount; ++i ) {
                            strs[i] = (byte)mRecgs[i];
                        }
                        try{
                            String strReg = new String(strs, "UTF8");

                            //Todo 检测接收到的是否正确
                            if (msgIsRight(strReg)){
                                receiveVoice.stop();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rippleBackground.stopRippleAnimation();
                                    }
                                });
                                updateBadge("正在连接目标热点");
                                wifiHotConnect(strReg);
                            }
                        }catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
    };

    private boolean msgIsRight(String s){
        //Todo 检测接收到的是否正确
        return true;
    }

    String SSID;
    String PASSWORD;
    private NetworkConnectChangedReceiver myBroadcastReceiver;
    WifiManager wifiManager;
    private void wifiHotConnect(String s){
        SSID = myWifiManager.wifiHotPrefix+s.substring(0,4);
        PASSWORD = s.substring(4);
        myBroadcastReceiver = new NetworkConnectChangedReceiver();
        updateUI(SSID+" "+PASSWORD);
        myConnectWifi();
    }
    private void myConnectWifi(){
        //进入此函数，wifi目前肯定是关闭的
        //打开wifi
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        wifiManager.setWifiEnabled(true);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(myBroadcastReceiver, filter);
    }
    private class NetworkConnectChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState)
                {
                    case WifiManager.WIFI_STATE_ENABLED:
                        if (!wifiManager.isWifiEnabled()) return;

                        unregisterReceiver(myBroadcastReceiver);
                        //到这才真正的去连接wifi

                        myWifiManager.toConnectWifiHot(getBaseContext(),SSID,PASSWORD);
                        //监听CONNECTED，判断是不是连接上了目标wifi
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                        registerReceiver(myBroadcastReceiver, filter);
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        if (wifiManager.isWifiEnabled()) return;
                        unregisterReceiver(myBroadcastReceiver);

                        myConnectWifi();
                        break;
                }
            }
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction()))
            {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra)
                {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    switch (networkInfo.getState())
                    {
                        case CONNECTED:
                            if (wifiManager.getConnectionInfo().getSSID().contains(SSID)){


                                updateBadge("正在向目标发送回应");
                                unregisterReceiver(myBroadcastReceiver);

                                new Thread(new Runnable() {
                                    Socket socket;
                                    @Override
                                    public void run() {
                                        int t=wifiManager.getDhcpInfo().serverAddress;
                                        MyWifiManager.setSERVERIP(MyWifiManager.intToIp(t));
                                        //Content.getContentInstance(getBaseContext()).setSERVERIP(Content.intToIp(t));
                                        try {
                                            //Thread.sleep(1000);//强行拖延两秒。。免得有的性能好的手机
                                            socket = new Socket(MyWifiManager.getSERVERIP(),MyWifiManager.getSERVERPORT());
                                            updateUI(""+MyWifiManager.getSERVERIP()+MyWifiManager.getSERVERPORT());

                                            updateBadge("发送回应成功");
                                            //获得server分配的设备信息
                                            //获取输入流
                                            DataInputStream dis = null;
                                            try{
                                                dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                                            }catch (IOException e){
                                                e.printStackTrace();

                                                return;
                                            }

                                            int uid;
                                            long port;
                                            try {
                                                uid = dis.read();
                                                port = dis.readLong();

                                                Device clientDevice = new Device(socket,uid,port);
                                                TheClientDevice.cilentDevice = clientDevice;
                                                Intent intent1 = new Intent(ReceiveSignalActivity.this,ReceiveFileInfoActivity.class);
                                                //intent1.putExtra("uid",uid);
                                                //intent1.putExtra("port",port);
                                                startActivity(intent1);
                                                finish();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }



                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            updateUI(e.getMessage());
                                            run();
                                        }

                                    }
                                }).start();
                            }else {
                                wifiManager.disconnect();
                                myWifiManager.toConnectWifiHot(getBaseContext(),SSID,PASSWORD);
                            }
                            break;
                    }
                }
            }

        }
    }

    @Override
    public void onPause(){
        super.onPause();

        receiveVoice.stop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        receiveVoice.uninit();
    }







}

