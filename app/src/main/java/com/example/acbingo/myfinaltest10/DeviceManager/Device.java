package com.example.acbingo.myfinaltest10.DeviceManager;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by tfuty on 2016-05-24.
 */
public class Device {
    Socket socket;
    int uid;
    long port;

    public ArrayList<Integer> sendFileList;

    public Device (Socket socket){
        uid = AllClientDevices.uidCnt++;
        port = AllClientDevices.portCnt+=5;
        this.socket = socket;
    }
    public Device (Socket socket,int uid,long port){
        this.socket = socket;
        this.uid = uid;
        this.port = port;
    }

    public void setSendFileList(ArrayList<Integer> list){
        sendFileList = list;
    }

    public int getUid(){
        return uid;
    }
    public long getPort(){
        return port;
    }
    public Socket getSocket(){
        return socket;
    }
    public void sendback(){
        new Thread(new Runnable() {
            DataOutputStream dos;
            @Override
            public void run() {
                try {
                    dos = new DataOutputStream(socket.getOutputStream());//获得输出流
                    dos.write(uid);
                    dos.flush();
                    dos.writeLong(port);
                    dos.flush();
                    Log.d("myacbingo",""+uid+" "+port);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
