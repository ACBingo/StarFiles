package com.example.acbingo.myfinaltest10.activitys;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.acbingo.myfinaltest10.DeviceManager.AllClientDevices;
import com.example.acbingo.myfinaltest10.FileChooseService.FileChoose;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by tfuty on 2016-06-08.
 * /
 * Created by tfuty on 2016-05-28.
 *
 * allFiles[i]表示第i个文件的File实例
 * allFilesDirectory[i]表示第i个文件的绝对路径
 * DEFAULT_POOL_SIZE为默认每个Device线程的线程数量，最大为4
 * all_Devices_File_Choose[i][j]表示第i个设备的第j个文件传或不传
 * all_Devices_File_TotalLength[i]表示第i个设备的发送总长度
 * now_Devices_Sending_fileNum[i]表示当前第i个设备正在发送的文件序号
 * now_Devices_Sending_length[i]表示当前第i个设备正在发送的now_Devices_Sending_fileNum[i]文件已发送长度
 * all_Device_SocketPool[i]表示第i个设备的socketPool
 */

public class SendFileService {
    public final static int SERVICE_IS_READY = 100;

    private Context context;
    private Handler UIhandler;

    private ArrayList<File> allFiles;
    private ArrayList<String> allFilesDirectory = new ArrayList<String>();
    private ArrayList<Integer> NowSendFileOnDevice;

    private ArrayList<ArrayList<Socket>> all_Device_SocketPool = new ArrayList<>();

    private boolean[][] all_Devices_File_Choose = new boolean[30][100];
    private long[] all_Devices_File_TotalLength = new long[30];
    private int[] now_Devices_Sending_fileNum = new int[30];
    private int[] now_Devices_Sending_length = new int[30];

    private int DEFAULT_POOL_SIZE = 4;
    private int DEFAULT_BUFFER_SIZE = 1024*4;//4kb

    private int deviceCount;
    private int fileCount;
    private long TotalSize = 0;//所有设备的所有文件总大小

    private int allFileChooseCount = 0;
    private int theFileSendCount = 0;


    public SendFileService (Context context){
        this.context = context;

        allFiles = FileChoose.getFileChooseArrayList();
        for (int i=0;i<allFiles.size();i++){
            allFilesDirectory.add(allFiles.get(i).getAbsolutePath());
        }

        fileCount = allFiles.size();
        deviceCount = AllClientDevices.clientDevices.size();

    }

    public void setHandler(Handler handler){
        this.UIhandler = handler;
        begin();
    }

    private void begin()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*for (int k=0;k<deviceCount;k++){
                    int port = (int)AllClientDevices.clientDevices.get(k).getPort();
                    ArrayList<Socket>  SocketPool = new ArrayList<>();
                    try{
                        for (int i=0;i<DEFAULT_POOL_SIZE;i++){
                            ServerSocket serverSocket = new ServerSocket(port+i+1);
                            Socket tmpSocket = serverSocket.accept();

                            //tmpSocket.setSendBufferSize(DEFAULT_BUFFER_SIZE);
                            //tmpSocket.setTcpNoDelay(true);//不启用nagle算法

                            Log.d("thisacbingo","第i个端口已经连上"+i);
                            SocketPool.add(tmpSocket);
                            serverSocket.close();
                        }}catch (IOException e){
                        //updateUI(e.getMessage());
                    }
                    all_Device_SocketPool.add(SocketPool);
                }*/
                init();
            }
        }).start();
    }

    private void init(){
        NowSendFileOnDevice = new ArrayList<>();
        for (int i=0;i<deviceCount;i++)
            NowSendFileOnDevice.add(0);
        for (int i=0;i<deviceCount;i++){
            new SendFilesToOneDevice(i).start();
        }
    }

    private class SendFilesToOneDevice extends Thread{
        Socket socket;
        int uid;
        int port;
        int k;
        DataOutputStream sos = null;
        DataInputStream sis = null;
        ArrayList<Integer> sendFileList = new ArrayList<>();
        ArrayList<Boolean> allFilesChoose = new ArrayList<Boolean>();
        int NowSendFile;
        SendFilesToOneDevice(int k) {
            socket = AllClientDevices.clientDevices.get(k).getSocket();
            uid = AllClientDevices.clientDevices.get(k).getUid();
            port = (int)AllClientDevices.clientDevices.get(uid).getPort();
            this.k = k;
            //Log.d("thisdebug",""+uid+" "+k);
        }
        @Override
        public void run(){
            try {
                //Todo 所有文件发送完成后关闭socket
                sos = new DataOutputStream(socket.getOutputStream());//获得输出流
                sis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                //发送一个0过去，通知客户端开始接收文件
                sos.write(0);
                sos.flush();

                //将表发送过去
                //updateUI("数量"+fileCount);
                sos.write(fileCount);//先传数量
                sos.flush();
                for (String oneFileDirectory: allFilesDirectory){
                    File file = new File(oneFileDirectory);

                    sos.writeUTF(file.getName());//先传name
                    sos.flush();
                    sos.writeLong((long)file.length());//再传文件长度
                    sos.flush();
                }
                //updateUI("表发送完成");
                //收回来选择表
                for (int i=0;i<fileCount;i++){
                    Boolean t = sis.readBoolean();
                    allFilesChoose.add(t);

                    all_Devices_File_Choose[uid][i]=t;
                    all_Devices_File_TotalLength[uid] += allFiles.get(i).length();
/*
                    String s="第"+i+"个文件:";
                    if (t) s+="传";
                    else s+="不传";
                    //updateUI(s);*/
                }

                Message msg = new Message();
                msg.what = 2;
                msg.arg1 = uid;
                handler.sendMessage(msg);//从uid号client过来的选择表已经接收完毕

                NowSendFile = 0;
                //逐一的发送文件
                for (int i=0;i<fileCount;i++){
                    if (!allFilesChoose.get(i)) continue;//如果这个文件不发，就跳过
                    sendFileList.add(i);
                    TotalSize+=allFiles.get(i).length();

                    allFileChooseCount++;
                }

                AllClientDevices.clientDevices.get(uid).setSendFileList(sendFileList);
                sendFile(NowSendFile,uid);//开始发送第uid个设备的第一个文件

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket!=null)
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (sis!=null)
                    try {
                        sis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (sos!=null)
                    try {
                        sos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    private void sendFile(int fileNum,int uid){
        if (fileNum>=AllClientDevices.clientDevices.get(uid).sendFileList.size())
            return;

        int Port = (int)AllClientDevices.clientDevices.get(uid).getPort();

        File file = new File(allFilesDirectory.get(AllClientDevices.clientDevices.get(uid).sendFileList.get(fileNum)));

        long fileLength = (long) file.length();

        long blockLength = fileLength / DEFAULT_POOL_SIZE;
        for (int i = 0; i<DEFAULT_POOL_SIZE;i++){
            long beginPosition = i * blockLength;//每条线程下载的开始位置
            long endPosition = (i + 1) * blockLength;//每条线程下载的结束位置
            if (i == (DEFAULT_POOL_SIZE - 1)) {
                endPosition = fileLength;
                //如果整个文件的大小不为线程个数的整数倍，则最后一个线程的结束位置即为文件的总长度
            }

            new SendFileByBlockTheard(Port+i+1,file,beginPosition,endPosition,uid,fileNum).start();
        }
    }

    private class SendFileByBlockTheard extends Thread{
        private int Port;
        private long beginPosition;
        private long endPosition;
        private File file;
        private DataInputStream filedis = null;
        private DataOutputStream dos = null;
        private ServerSocket serverSocket;
        private Socket socket;
        private int uid;
        private int fileNum;
        SendFileByBlockTheard(int port,File f,long b,long e,int uid,int fileNum){
            //socket = s;
            file =f;
            beginPosition = b;
            endPosition = e;
            this.uid = uid;
            this.fileNum = fileNum;
            this.Port = port;
        }
        @Override
        public void run(){
            try{

                serverSocket = new ServerSocket(Port);
                socket = serverSocket.accept();

                socket.setSendBufferSize(DEFAULT_BUFFER_SIZE);

                socket.setSoTimeout(10*1000);//10秒超时时间

                dos = new DataOutputStream(socket.getOutputStream());//获得输出流
                filedis = new DataInputStream(new BufferedInputStream(
                        new FileInputStream(file)));

                skipFully(filedis,beginPosition);

                //一次发4k
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int hasRead = 0;
                long nowLength = 0;

                while (nowLength<(endPosition-beginPosition)&&
                        (hasRead = filedis.read(buffer))>0){
                    dos.write(buffer,0,hasRead);
                    nowLength+=hasRead;

                    Message msg= new Message();
                    msg.obj = hasRead;
                    msg.arg1 = uid;
                    msg.arg2 = AllClientDevices.clientDevices.get(uid).sendFileList.get(fileNum);
                    //Log.d("myfilenum",""+AllClientDevices.clientDevices.get(uid).sendFileList.get(fileNum));
                    msg.what = 11;
                    handler.sendMessage(msg);//把当前已经读到的进度更新上去
                }

                dos.flush();
                Message msg= new Message();
                msg.obj = nowLength;
                msg.arg1 = uid;
                msg.arg2 = AllClientDevices.clientDevices.get(uid).sendFileList.get(fileNum);
                msg.what = 12;
                handler.sendMessage(msg);
                //表示当前线程已经执行完毕
            } catch (IOException e) {
                e.printStackTrace();

                //Todo
                //UIhandler.sendEmptyMessage(SendFileInfoActivity.Failed);

                //updateUI(e.getMessage());
            } finally {
                try {
                    if (serverSocket!=null)
                        serverSocket.close();

                    /*if (dos!=null)
                        dos.close();*/
                    if (filedis!=null)
                        filedis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    //updateUI(e.getMessage());
                }
            }
        }
    }

    private static void skipFully(DataInputStream in,long bytes) throws IOException{
        long remainning = bytes;
        long len = 0;
        while (remainning>0){
            len = in.skip(remainning);
            remainning -= len;
        }
    }

    private Handler handler = new Handler(){

        int NowTotalProgress = 0;
        int NowFileProgress = 0;
        long nowTotalReadLength = 0;
        int cnt=0;


        long TotalReadLength = 0;

        long fileReadLength = 0;

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 2:
                {
                    Message UImessage = new Message();
                    UImessage.arg1 = msg.arg1;//deviceid
                    UImessage.what = SERVICE_IS_READY;
                    UIhandler.sendMessage(UImessage);
                    break;
                }
                case 12://某个线程发送结束
                {
                    cnt++;
                    if (cnt == DEFAULT_POOL_SIZE) {//当所有线程发送结束时，代表该文件发送结束

                        theFileSendCount++;
                        if (theFileSendCount>=allFileChooseCount){
                            UIhandler.sendEmptyMessage(SendFileInfoActivity.ALLEND);
                        }

                        Message UImessage = new Message();
                        UImessage.arg1 = msg.arg1;//deviceid
                        UImessage.arg2 = msg.arg2;//fileid
                        UImessage.what = SendFileInfoActivity.END;
                        UIhandler.sendMessage(UImessage);

                        fileReadLength = 0;
                        NowFileProgress = 0;

                       // updateUI("第" + "文件发送完毕");
                        int tmp = NowSendFileOnDevice.get(msg.arg1);
                        if (tmp < AllClientDevices.clientDevices.get(msg.arg1).sendFileList.size()) {
                            {
                                tmp++;
                                NowSendFileOnDevice.set(msg.arg1, tmp);
                                sendFile(tmp, msg.arg1);
                            }
                        } else {
                            Message message = new Message();
                            message.arg1 = msg.arg1;
                            message.arg2 = msg.arg2;
                            msg.what = 13;
                            handler.sendMessage(message);
                            //某设备全部文件发送完毕
                        }
                        cnt = 0;
                    }
                    break;
                }
                case 11: {
                    //处理当前的进度
                    nowTotalReadLength += (int) msg.obj;

                    long NowFileLength = allFiles.get(msg.arg2).length();

                    fileReadLength += (int) msg.obj;


                    if (((double) fileReadLength / NowFileLength * 100) - NowFileProgress > 1) {


                        Message UImessage = new Message();
                        Bundle te = new Bundle();
                        te.putLong(SendFileInfoActivity.UPDATE_ACCEPT_LEN, fileReadLength);
                        UImessage.setData(te);
                        UImessage.arg1 = msg.arg1;
                        UImessage.arg2 = msg.arg2;
                        UImessage.what = SendFileInfoActivity.UPDATE;
                        UIhandler.sendMessage(UImessage);

                        NowFileProgress = (int) ((double) fileReadLength / NowFileLength * 100);
                        if (NowFileProgress > 100) NowFileProgress = 100;


                        //waveLoadingView.setProgressValue(NowProgress);
                        //waveLoadingView.setCenterTitle(""+NowProgress+"%");
                    }

                    if (((double) nowTotalReadLength / TotalSize * 100) - NowTotalProgress > 1){
                        Message UImessage = new Message();
                        Bundle te = new Bundle();

                        //Todo ?????????这里的fileReadLength 你用来干啥的？
                        te.putLong(SendFileInfoActivity.UPDATE_ACCEPT_LEN, fileReadLength);
                        UImessage.setData(te);
                        UImessage.arg1 = msg.arg1;
                        UImessage.arg2 = msg.arg2;
                        UImessage.what = SendFileInfoActivity.UPDATETOTAL;
                        NowTotalProgress = (int) ((double) nowTotalReadLength / TotalSize * 100);
                        if (NowTotalProgress > 100) NowTotalProgress = 100;
                        UImessage.obj = NowTotalProgress;
                        UIhandler.sendMessage(UImessage);

                    }


                    //Todo 进度更新
                    //updateUI("第"+NowSendFile+"文件已经发送了"+(long)msg.obj);

                    break;
                }
                case 13:
                    //Todo 统计发送完毕的设备数量，
                    break;
                case 14:
                    //Todo 全部发送完毕
                    break;
            }
        }
    };

    private void updateUI(String s){
        Log.d("myacbingo",s);
    }

    public int getFileCount(){

        return allFiles.size();
    }

    public int getDeviceCount(){
        return deviceCount;
    }

    public String getFileName(int i){
        return allFiles.get(i).getName();
    }

    public long getFileSize(int i){
        return allFiles.get(i).length();
    }

    public boolean[] getDevices_File_Choose(int i) {
        return all_Devices_File_Choose[i];
    }
    public boolean[][] getAll_Devices_File_Choose(){
        return all_Devices_File_Choose;
    }

}
