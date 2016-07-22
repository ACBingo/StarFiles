package com.example.acbingo.myfinaltest10.PCTransmission;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.acbingo.myfinaltest10.FileManagerService.FileManagerMainActivity;
import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.activitys.ReceiveFileService;
import com.jaeger.library.StatusBarUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import me.itangqi.waveloadingview.WaveLoadingView;
import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by tfuty on 2016-06-13.
 */
public class FileReceiveFromPCActivity extends AppCompatActivity {

    WaveLoadingView waveLoadingView;
    BadgeView badgeView;

    public String Path;

    int flag = 0;

    Button back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_receive_from_pc);

        StatusBarUtil.setTransparent(this);

        waveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        badgeView = (BadgeView) findViewById(R.id.badge_view);

        back_btn = (Button) findViewById(R.id.back_btn);

        Path = ReceiveFileService.getFileSavePath();

        handler.sendEmptyMessage(1);
        receive();

        badgeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag==1){
                    finish();
                }
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag==1){
                    Intent te = new Intent(getApplicationContext() , FileManagerMainActivity.class);
                    te.putExtra("path", ReceiveFileService.getFileSavePath());
                    startActivity(te);
                    finish();
                }
            }
        });

    }

    private void receive(){
        new Thread(new Runnable() {
            Socket socket;
            @Override
            public void run() {

                socket = PCMainActivity.socket;

                try {
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                    int bufferSize = 1024*4;
                    byte[] buf = new byte[bufferSize];

                    try {
                        socket.setSoTimeout(60*1000);//设置超时时间
                    } catch (SocketException e) {
                        e.printStackTrace();
                        //Log.d(TAG,"setTimeout failed");
                    }

                    String savePath=Path;
                    String fileName =dis.readUTF();
                    savePath +='/'+fileName;

                    Message message = new Message();
                    message.what = 2;
                    message.obj = fileName;
                    handler.sendMessage(message);

                    DataOutputStream fileOut = new DataOutputStream(
                            new BufferedOutputStream(new BufferedOutputStream(
                                    new FileOutputStream(savePath))));

                    long fileLength = dis.readLong();

                    long nowFileReadLength = 0;
                    int NowFileProgress = 0;

                    while (true){
                        int readlength = 0;
                        if (dis!=null){
                            readlength = dis.read(buf);
                        }
                        if (readlength == -1){//读完毕
                            break;
                        }

                        nowFileReadLength += readlength;

                        if (((double) nowFileReadLength / fileLength * 100) - NowFileProgress > 1){
                            NowFileProgress = (int)((double) nowFileReadLength / fileLength * 100);

                            if (NowFileProgress>100) NowFileProgress = 100;

                            Message message1 = new Message();
                            message1.what = 3;
                            message1.obj = NowFileProgress;
                            handler.sendMessage(message1);

                        }

                        fileOut.write(buf,0,readlength);
                    }

                    handler.sendEmptyMessage(4);

                    dis.close();
                    fileOut.close();
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    badgeView.setValue("连接成功,等待PC发送文件");
                    break;
                case 2:
                    String s = (String) msg.obj;
                    badgeView.setValue("正在接收文件:"+s);

                    break;
                case 3:
                    int NowFileProgress = (int)msg.obj;
                    waveLoadingView.setCenterTitle(NowFileProgress+" %");
                    waveLoadingView.setProgressValue(NowFileProgress);
                    break;
                case 4:
                    waveLoadingView.setCenterTitle(100+" %");
                    waveLoadingView.setProgressValue(100);
                    badgeView.setValue("文件接收完成,点击返回到主页面");
                    Toast.makeText(getBaseContext(),"文件保存在了:"+Path,Toast.LENGTH_LONG).show();
                    flag=1;
                    back_btn.setVisibility(View.VISIBLE);
                    break;
                case 0:
                    badgeView.setValue("传输失败!请检查网络后重试");
                    break;
            }
        }
    };

}
