package com.example.acbingo.myfinaltest10.WanTransmission;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by tfuty on 2016-06-12.
 */
public class Client_send_service {
    static BufferedWriter myLogOut = null;
    static int ServerPort = 44180;
    static int TransPort;
    static String ServerIp = "115.159.195.134";
    static String Path;
    static File theChooseFile;
    private Context context;
    private Handler handler;

    private int FailedCnt = 0;

    public Client_send_service(Context context,Handler handler){
        Path = Environment.getExternalStorageDirectory().getPath()+'/';

        this.context = context;

        this.handler = handler;

        //Todo
        //Path += '/'+"test.txt";

        init();
    }

    public void init(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ServerIp,ServerPort);
                    socket.setSoTimeout(60*1000);

                    DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    TransPort = dis.readInt();

                    //send file
                    dos.writeBoolean(true);

                    handler.sendEmptyMessage(UpActivity.Server_is_read_to_receive);

                    socket.close();
                    dis.close();
                    dos.close();
                } catch (IOException e) {
                    handler.sendEmptyMessage(UpActivity.Server_Fail);
                    //e.printStackTrace();
                }

            }
        }).start();
    }
    public void sendFile(File file){

        handler.sendEmptyMessage(UpActivity.Update_begin);
        sendFile(file,TransPort);
    }

    private void sendFile(final File file,final int port){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {

                    Socket socket = new Socket(ServerIp,TransPort);
                    socket.setSoTimeout(60*1000);

                    DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    String fileCode = dis.readUTF();

                    Message msg = new Message();
                    msg.what = UpActivity.FileCode;
                    msg.obj = fileCode;
                    handler.sendMessage(msg);

                    //File file = new File(Path+fileName);
                    DataInputStream fileis = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getAbsolutePath())));

                    int bufferSize = 1024*128;
                    byte[]bufArray = new byte[bufferSize];

                    dos.writeUTF(file.getName());
                    dos.flush();
                    dos.writeLong(file.length());
                    dos.flush();

                    long nowreadlength = 0;
                    int nowProgress = 0;

                    while (true){
                        int readLength = -1;
                        if (fileis!=null){
                            readLength = fileis.read(bufArray);
                        }
                        if (readLength==-1){
                            break;
                        }

                        nowreadlength +=readLength;
                        if ( ((double)nowreadlength / file.length())*100 - nowProgress >1 ){
                            nowProgress =(int) (((double)nowreadlength / file.length())*100);
                            Log(""+((double)nowreadlength / file.length())*100);
                            Log(" "+nowProgress);

                            Message msg1 = new Message();
                            msg1.what = UpActivity.Update_progress;
                            msg1.arg1 = nowProgress;
                            handler.sendMessage(msg1);
                        }
                        dos.write(bufArray,0,readLength);
                    }
                    dos.flush();

                    handler.sendEmptyMessage(UpActivity.Update_END);

                    socket.close();
                    dos.close();
                    dis.close();
                    fileis.close();


                } catch (IOException e) {
                    FailedCnt++;
                    if (FailedCnt<120) {

                        e.printStackTrace();
                        Log(e.getMessage());

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        run();
                    }else{
                        handler.sendEmptyMessage(UpActivity.Server_Fail);
                    }
                }

            }
        }).start();
    }

    public void Log(String s){
        Log.d("myacbingo",s);
    }
}
