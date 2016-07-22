package com.example.acbingo.myfinaltest10.activitys;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.acbingo.myfinaltest10.DeviceManager.TheClientDevice;
import com.example.acbingo.myfinaltest10.MyWifiManager.MyWifiManager;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by tfuty on 2016-06-10.
 * <p/>
 * allFilesName[i]表示第i个文件的名字
 * allFilesSize[i]表示第i个文件的大小
 * allFilesChoose[i]表示第i个文件是否接收
 * <p/>
 * receiveFileList是接收文件队列。
 * 最终接收文件的序号放在receiveFileList中。顺序的读取receiveFileList的每一项进行接收
 * 当后台service已经准备好时，发送FILE_TABLE_IS_READY
 */
public class ReceiveFileService {

    public static final int SERVICE_IS_READY = 100;
    public static final int RECEVIE_TABLE = 33;
    public static final String FILELIST = "filelist";
    public static final int STRAT_RECEIVE = 3;

    private int uid;
    private int port;
    private Socket socket;
    private DataOutputStream socketos = null;
    private DataInputStream socketis = null;

    private int fileCount;
    private long TotalSize;

    private long TotalChooseSize;

    private int allFileChooseCount = 0;
    private int theFileReceiveCount = 0;

    private ArrayList<String> allFilesName = new ArrayList<String>();
    private ArrayList<Long> allFilesSize = new ArrayList<Long>();
    private ArrayList<Long> allFilesHasRead = new ArrayList<>();
    private ArrayList<Boolean> allFilesChoose = new ArrayList<Boolean>();
    private ArrayList<Integer> receiveFileList = new ArrayList<Integer>();

    private ArrayList<Socket> socketPool = new ArrayList<>();

    private int NowReceiveFile = 0;
    private int DEFAULT_POOL_SIZE = 4;
    private int DEFAULT_BUFFER_SIZE = 1024 * 4;//4kb

    private static String fileSavePath = null;

    private Context context;
    private Handler UIhandler;

    public ReceiveFileService(Context context) {
        this.context = context;

        //Todo 做成可以设置的
        if (fileSavePath == null)
            setFileSavePath(Environment.getExternalStorageDirectory().getPath() + "/StarFiles_Save/");
        uid = TheClientDevice.cilentDevice.getUid();
        port =(int) TheClientDevice.cilentDevice.getPort();
        socket = TheClientDevice.cilentDevice.getSocket();

        for (int i = 0; i < fileCount; i++) {
            allFilesHasRead.add((long) 0);
        }

    }



    public void setHandler(Handler handler) {
        UIhandler = handler;
        begin();
    }

    public void begin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //建立socketPool
                //Todo 显示连接初始化
                /*for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
                    while (true) {
                        try {
                            Socket tmpsocket = new Socket(MyWifiManager.getSERVERIP(), (int) port + i + 1);

                            //tmpsocket.setReceiveBufferSize(DEFAULT_BUFFER_SIZE);
                            //tmpsocket.setTcpNoDelay(true);//不启用nagle算法

                            socketPool.add(tmpsocket);
                            break;
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                            //updateUI("IP异常");
                            try {
                                Thread.sleep(100);
                            } catch (Exception e2) {
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            //updateUI("连接超时,请检查IP是否正确或者服务端是否打开");
                            try {
                                Thread.sleep(100);
                            } catch (Exception e2) {
                            }
                        }
                    }
                }*/

                init();

                //Todo 关闭所有socket
            }
        }).start();
    }

    private void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    socketis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    socketis.read();

                    //发送端发来发送请求
                    handler.sendEmptyMessage(0);
                    fileCount = socketis.read();

                    handler.sendEmptyMessage(1);
                    for (int i = 0; i < fileCount; i++) {
                        String name = socketis.readUTF();
                        allFilesName.add(name);
                        Long size = socketis.readLong();
                        allFilesSize.add(size);
                        TotalSize += size;

                    }
                    handler.sendEmptyMessage(2);//此时通知用户选择想要接收的文件
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private Handler handler = new Handler() {
        int TaskCnt = 0;
        int NowFileProgress = 0;
        int NowTotalProgress = 0;
        long nowTotalReadLength = 0;
        long nowFileLength = 0;
        long nowFileReadLength = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //waveLoadingView.setCenterTitle("请选择需要接收的文件");
                    break;
                case 1:
                    //Todo 文件总数为
                    break;
                case 2:

                    //Todo 弹出dialog，选择要接收的文件，选择完毕后发送3。
                    // 通过向ui界面传递信息实现
                    Message te = new Message();
                    te.what = RECEVIE_TABLE;
                    Bundle bun = new Bundle();
                    bun.putStringArrayList(FILELIST, allFilesName);
                    te.setData(bun);
                    UIhandler.sendMessage(te);
                    //handler.sendEmptyMessage(3);
                    break;
                case STRAT_RECEIVE:

                    boolean[] res = msg.getData().getBooleanArray(ReceiveFileInfoActivity.CHOICE_LIST);
                    allFilesChoose = new ArrayList<Boolean>();
                    for(boolean x : res)
                                allFilesChoose.add(x);

                    for (int i=0;i<allFilesChoose.size();i++){
                        if (allFilesChoose.get(i)){
                            TotalChooseSize+=allFilesSize.get(i);
                            allFileChooseCount++;
                        }
                    }

                    sendFilesChoose();
                    UIhandler.sendEmptyMessage(SERVICE_IS_READY);


                    break;
                case 11:

                    nowFileLength = allFilesSize.get(msg.arg1);

                    nowFileReadLength += (int) msg.obj;
                    nowTotalReadLength += (int) msg.obj;


                    //处理当前的进度
                    if (((double) nowFileReadLength / nowFileLength * 100) - NowFileProgress > 1){


                        NowFileProgress = (int)((double) nowFileReadLength / nowFileLength * 100);

                        if (NowFileProgress>100) NowFileProgress=100;


                        Message UImessage = new Message();
                        UImessage.what = ReceiveFileInfoActivity.UPDATE;
                        UImessage.obj = NowFileProgress;
                        UImessage.arg2 = msg.arg1;//fileNum
                        UIhandler.sendMessage(UImessage);


                    }

                    if (((double) nowTotalReadLength / TotalChooseSize * 100) - NowTotalProgress > 1) {

                        NowTotalProgress =(int) ((double) nowTotalReadLength / TotalChooseSize * 100);

                        if (NowTotalProgress>100) NowTotalProgress=100;

                        Message UImessage = new Message();
                        UImessage.what = ReceiveFileInfoActivity.UPDATETOTAL;
                        UImessage.obj = NowTotalProgress;
                        UIhandler.sendMessage(UImessage);
                    }
                    break;

                case 12:
                    TaskCnt++;
                    if (TaskCnt == DEFAULT_POOL_SIZE) {

                        theFileReceiveCount++;

                        if(theFileReceiveCount>=allFileChooseCount){
                            UIhandler.sendEmptyMessage(ReceiveFileInfoActivity.ALLEND);
                        }

                        Message UImessage = new Message();
                        UImessage.what = ReceiveFileInfoActivity.END;
                        UImessage.arg2 = msg.arg1;//fileNum
                        UIhandler.sendMessage(UImessage);

                        //Todo 当前的文件接收完毕
                        nowFileReadLength = 0;
                        NowFileProgress = 0;

                        if (NowReceiveFile < receiveFileList.size()) {
                            try {
                                receiveFile(receiveFileList.get(NowReceiveFile++));//发送下一个文件
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        TaskCnt = 0;
                    }
                    break;
            }
        }
    };

    private void sendFilesChoose() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketos = new DataOutputStream(socket.getOutputStream());//获得输出流
                    for (int i = 0; i < fileCount; i++) {
                        socketos.writeBoolean(allFilesChoose.get(i));
                        socketos.flush();
                    }


                    NowReceiveFile = 0;
                    for (int i = 0; i < fileCount; i++) {
                        if (!allFilesChoose.get(i)) continue;
                        receiveFileList.add(i);
                    }
                    receiveFile(NowReceiveFile++);//开始收第一个文件

                    //这些就都可以关闭了，去连接新的socket进行多线程传输
                    socket.close();
                    socketos.close();
                    socketis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void receiveFile(int k) throws IOException {
        if (k >= receiveFileList.size()) return;

        String savaPath = fileSavePath + allFilesName.get(receiveFileList.get(k));

        long fileLength = (long) allFilesSize.get(receiveFileList.get(k));
        long blockLength = fileLength / DEFAULT_POOL_SIZE;

        //创建文件
        RandomAccessFile file = new RandomAccessFile(savaPath, "rw");
        file.setLength(fileLength);
        file.close();



        for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
            long beginPosition = i * blockLength;//每条线程下载的开始位置
            long endPosition = (i + 1) * blockLength;//每条线程下载的结束位置
            if (i == (DEFAULT_POOL_SIZE - 1)) {
                endPosition = fileLength;
                //如果整个文件的大小不为线程个数的整数倍，则最后一个线程的结束位置即为文件的总长度
            }

            new ReceiveFileByBlockTheard(port+i+1, savaPath, beginPosition, endPosition, receiveFileList.get(k)).start();
        }
    }

    private class ReceiveFileByBlockTheard extends Thread {
        private int Port;
        private long beginPosition;
        private long endPosition;
        private String savePath;
        private RandomAccessFile currentPart;
        private DataOutputStream dos = null;
        private DataInputStream dis = null;
        private Socket socket;

        private int fileNum;

        ReceiveFileByBlockTheard(int port, String s, long b, long e, int fileNum) {
            //this.socket = socket;
            this.Port = port;
            savePath = s;
            beginPosition = b;
            endPosition = e;
            this.fileNum = fileNum;
        }

        @Override
        public void run() {

            try {

                int trytime = 0;

                while (true) {
                    try {
                        socket = new Socket(MyWifiManager.getSERVERIP(), Port);
                        break;
                    }
                    catch (IOException e) {

                        trytime++;

                        if (trytime<100){
                            e.printStackTrace();

                            try {
                                Thread.sleep(100);
                            } catch (Exception e2) {
                            }
                        }else{
                            break;
                        }

                    }
                }

                socket.setSoTimeout(10*1000);//10秒超时时间

                socket.setReceiveBufferSize(DEFAULT_BUFFER_SIZE);

                dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dos = new DataOutputStream(socket.getOutputStream());//获得输出流

                currentPart = new RandomAccessFile(savePath, "rw");
                currentPart.seek(beginPosition);

                //一次收4k
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int hasRead = 0;
                long nowLength = 0;

                while (nowLength < (endPosition - beginPosition) &&
                        (hasRead = dis.read(buffer)) > 0) {
                    currentPart.write(buffer, 0, hasRead);
                    nowLength += hasRead;

                    Message msg = new Message();
                    msg.obj = hasRead;
                    msg.arg1 = fileNum;
                    msg.what = 11;
                    handler.sendMessage(msg);//把当前已经读到的进度更新上去
                }

                Message msg = new Message();
                msg.arg1 = fileNum;
                msg.what = 12;
                handler.sendMessage(msg);//发送12，代表已经接收完了

            } catch (IOException e) {
                e.printStackTrace();

                //Todo
                //UIhandler.sendEmptyMessage(ReceiveFileInfoActivity.Failed);
            } finally {
                try {

                    /*if (socketis!=null)
                        socketis.close();
                    if (socketos!=null)
                        socketos.close();
                    if (dos!=null)
                        dos.close();
                    if (dis!=null)
                        dis.close();*/

                    if (currentPart != null)
                        currentPart.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateUI(String s) {
        Log.d("myacbingo", s);
    }

    public String getFileName(int i) {
        return allFilesName.get(i);
    }

    public long getFileSize(int i) {
        return allFilesSize.get(i);
    }

    public boolean getFileChoose(int i) {
        return allFilesChoose.get(i);
    }

    public static String getFileSavePath() {
        if (fileSavePath==null)
            fileSavePath = Environment.getExternalStorageDirectory().getPath() + "/StarFiles_Save/";
        return fileSavePath;
    }

    public int getFileCount() {
        return this.fileCount;
    }
    public Handler getHandler(){ return this.handler;}

    public static void setFileSavePath(String path) {
        if (path.charAt(path.length() - 1) != '/') path += '/';
        fileSavePath = path;
        File file = new File(fileSavePath);
            //判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
