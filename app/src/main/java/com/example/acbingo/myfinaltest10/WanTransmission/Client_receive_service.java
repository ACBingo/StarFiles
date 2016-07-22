package com.example.acbingo.myfinaltest10.WanTransmission;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.example.acbingo.myfinaltest10.activitys.ReceiveFileService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by tfuty on 2016-06-12.
 */
public class Client_receive_service {
    static BufferedWriter myLogOut = null;
    static int ServerPort = 44180;
    public static int TransPort;
    static String ServerIp = "115.159.195.134";
    static String Path;

    private Handler handler;

    Socket socket = null;
    DataInputStream dis = null;
    DataOutputStream dos = null;
    public Client_receive_service (Handler handler){
        Path = ReceiveFileService.getFileSavePath();

        this.handler = handler;

        init();
    }

    public void init(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socket = new Socket(ServerIp,ServerPort);

                    socket.setSoTimeout(60*1000);

                    dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    dos = new DataOutputStream(socket.getOutputStream());

                    TransPort = dis.readInt();
                    dos.writeBoolean(false);

                    handler.sendEmptyMessage(DownActivity.Server_is_ready);
                    /*Scanner str = new Scanner(System.in);
                    String code;
                    while(true){

                        //.d("请输入filecode:");
                        code = str.nextLine();
                        dos.writeUTF(code);
                        dos.flush();

                        if (dis.readBoolean()){
                            //Log.d("filecode is right,begin to receive");
                            break;
                        }else {
                            //Log("filecode is wrong,please type again");
                        }
                    }
                    str.close();
                    receiveFile(TransPort);

                    socket.close();
                    dis.close();
                    dos.close();*/
                } catch (Exception e){
                    handler.sendEmptyMessage(DownActivity.Failed);
                }
            }
        }).start();
    }

    public void begin(final String fileCode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (dos==null) {
                        throw new IOException();
                    }

                    dos.writeUTF(fileCode);
                    dos.flush();
                    if (dis.readBoolean()){
                        handler.sendEmptyMessage(DownActivity.File_Code_is_right);
                    }else{
                        handler.sendEmptyMessage(DownActivity.File_Code_is_wrong);
                    }
                } catch (IOException e) {
                    handler.sendEmptyMessage(DownActivity.Failed);
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void receiveFile(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Socket socket = new Socket(ServerIp, TransPort);


                    socket.setSoTimeout(60*1000);

                    int bufferSize = 1024*1024;
                    byte[] buf = new byte[bufferSize];

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                    String fileName = dis.readUTF();
                    fileName = fileName.substring(7);

                    Message message = new Message();
                    message.what = DownActivity.File_Name;
                    message.obj = fileName;
                    handler.sendMessage(message);


                    String savePath = Path+fileName;
                    DataOutputStream fileos = new DataOutputStream(
                            new BufferedOutputStream(new BufferedOutputStream(
                                    new FileOutputStream(savePath))));

                    long fileLength = dis.readLong();
                    //Log("filelength is"+fileLength);

                    Message message1 = new Message();
                    message1.what = DownActivity.File_Length;
                    message1.obj = fileLength;
                    handler.sendMessage(message1);

                    long nowReadLength = 0;
                    int nowProgress = 0;

                    while (true){
                        int readLength = -1;
                        if (dis!=null){
                            readLength = dis.read(buf);
                        }

                        if (readLength==-1){
                            break;
                        }

                        nowReadLength+=readLength;
                        if ( ((double)nowReadLength / fileLength)*100 - nowProgress >1 ){
                            nowProgress =(int) (((double)nowReadLength / fileLength)*100);
                            //Log(""+((double)nowReadLength / fileLength)*100);
                            //Log(" "+nowProgress);

                            Message msg1 = new Message();
                            msg1.what = DownActivity.Update_Progress;
                            msg1.arg1 = nowProgress;
                            handler.sendMessage(msg1);
                        }


                        fileos.write(buf,0,readLength);
                    }

                    handler.sendEmptyMessage(DownActivity.Update_End);


                    fileos.close();
                    dis.close();
                    dos.close();
                    socket.close();

                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    handler.sendEmptyMessage(DownActivity.Failed);
                    e.printStackTrace();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    handler.sendEmptyMessage(DownActivity.Failed);
                    e.printStackTrace();

                }
            }
        }).start();

    }
}
