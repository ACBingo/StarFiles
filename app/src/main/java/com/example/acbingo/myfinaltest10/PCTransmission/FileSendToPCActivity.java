package com.example.acbingo.myfinaltest10.PCTransmission;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.FileChooseService.ChooseFileActivity;
import com.example.acbingo.myfinaltest10.FileChooseService.FileChoose;
import com.jaeger.library.StatusBarUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import me.itangqi.waveloadingview.WaveLoadingView;
import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by tfuty on 2016-06-13.
 */
public class FileSendToPCActivity extends AppCompatActivity {
    String SERVERIP;

    Button btn_filechoose;
    WaveLoadingView waveLoadingView;
    BadgeView badgeView;

    Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_send_to_pc);

        StatusBarUtil.setTransparent(this);

        btn_filechoose = (Button) findViewById(R.id.choosefile);
        waveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        badgeView = (BadgeView) findViewById(R.id.badge_view);

        SERVERIP = getIntent().getStringExtra("serverip");



        btn_filechoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_filechoose.getText().equals("选择文件")){
                    Intent te = new Intent(getBaseContext(),ChooseFileActivity.class);
                    startActivityForResult(te, ChooseFileActivity.CHOOSEFILEACTIVITYRESULT);
                    btn_filechoose.setText("发送");
                }  else if (btn_filechoose.getText().equals("发送")){
                    if (FileChoose.getFileChooseCount()==0){
                        Toast.makeText(getBaseContext(),"请选择一个文件",Toast.LENGTH_SHORT).show();
                        btn_filechoose.setText("选择文件");
                    }else{
                        sendFile(FileChoose.getFileChooseArrayList().get(0).getPath());
                    }
                }  else if (btn_filechoose.getText().equals("退出")){
                    finish();
                }


            }
        });
    }

    public void updateUI(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    badgeView.setValue("连接成功");
                    break;
                case 2:
                    String s = (String) msg.obj;
                    badgeView.setValue("正在传输文件:"+s);
                    btn_filechoose.setText("正在发送");
                    break;
                case 3:
                    int NowFileProgress = (int)msg.obj;
                    waveLoadingView.setCenterTitle(NowFileProgress+" %");
                    waveLoadingView.setProgressValue(NowFileProgress);
                    break;
                case 4:
                    waveLoadingView.setCenterTitle(100+" %");
                    waveLoadingView.setProgressValue(100);
                    badgeView.setValue("文件传输完成");
                    btn_filechoose.setText("退出");
                    break;
                case 0:
                    badgeView.setValue("传输失败!请检查网络后重试");
                    break;
            }
        }
    };

    public void sendFile(final String filePath){
        final String ServerIP =SERVERIP;
        final int Port = 12345;
        new Thread(new Runnable() {
            DataInputStream dis = null;
            DataOutputStream dos = null;
            Socket socket = null;

            @Override
            public void run() {

                try{
                    File file = new File(filePath);
                   /*
                    updateUI("监听开始");
                    ss = new ServerSocket(Content.PORT);
                    socket = ss.accept();*/

                    //socket = new Socket(ServerIP,Port);

                    //handler.sendEmptyMessage(2);
                    //updateUI("连接成功");

                    socket = PCMainActivity.socket;

                    handler.sendEmptyMessage(1);
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));



                    int bufferSize = 1024*4;
                    byte[]bufArray = new byte[bufferSize];



                    dos.writeUTF(file.getName());//先传name
                    dos.flush();
                    //Log.d(TAG,"length");
                    dos.writeLong((long)file.length());//再传文件长度
                    dos.flush();

                    Message message = new Message();
                    message.what = 2;
                    message.obj = file.getName();
                    handler.sendMessage(message);

                    long nowFileReadLength = 0;
                    int NowFileProgress = 0;

                    while(true){//开始传文件

                        int readlength = 0;
                        if (dis!=null){
                            readlength = dis.read(bufArray);
                        }

                        if (readlength == -1){//没有读到结果
                            break;
                        }

                        nowFileReadLength += readlength;

                        if (((double) nowFileReadLength / file.length() * 100) - NowFileProgress > 1){
                            NowFileProgress = (int)((double) nowFileReadLength / file.length() * 100);

                            if (NowFileProgress>100) NowFileProgress = 100;

                            Message message1 = new Message();
                            message1.what = 3;
                            message1.obj = NowFileProgress;
                            handler.sendMessage(message1);

                        }

                        dos.write(bufArray,0,readlength);
                    }

                    handler.sendEmptyMessage(4);
                    dos.flush();
                    socket.close();
                    //ss.close();
                    dos.close();
                    dis.close();


                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    //Log.d(TAG,e.getMessage());
                    updateUI("文件未找到!");
                }catch (IOException e) {
                    e.printStackTrace();
                    //Log.d(TAG,e.getMessage());
                    handler.sendEmptyMessage(0);

                }finally {
                    //关闭所有连接
                    try{
                        if (dos!=null)
                            dos.close();
                    }catch (IOException e){}

                    try{
                        if (dis!=null)
                            dis.close();
                    }catch (IOException e){}

                    try{
                        if (socket!=null)
                            socket.close();
                    }catch (IOException e){}


                }
                //handler.sendEmptyMessage(1);
            }
        }).start();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case ChooseFileActivity.CHOOSEFILEACTIVITYRESULT:
                FileChoose myfilechoos = FileChoose.getInstance();
                ArrayList<String> all = data.getStringArrayListExtra(ChooseFileActivity.ALLSELECTEDPATH);
                for(String x : all){
                    int ok = 1;
                    ArrayList<File> nowall = FileChoose.getFileChooseArrayList();
                    for(File y  : nowall){
                        try {
                            if(y.getCanonicalPath().toString().equals(x) == true){
                                ok = 0;
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(ok == 0) continue;
                    myfilechoos.addFile(x);
                }
        }
    }

}
