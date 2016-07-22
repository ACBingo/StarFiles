package com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.acbingo.myfinaltest10.activitys.SendFileInfoActivity;


/**
 * Created by zhaoshuai on 2016/5/26.
 */
public class AcceptThread extends Thread {
    Handler handler;
    int pos;
    public AcceptThread(Handler hand, int position){
        handler = hand;
        pos = position;
    }
    public void run(){
        //AcceptInfo te = context.mList.get(pos);
        //te.setDownloadPercent(String.valueOf(i));
        //context.updateView(pos);
        for(int i = 0 ; i<=100; i++){
            Message message = new Message();
            message.what = SendFileInfoActivity.UPDATE;
            message.arg1 = pos;
            message.arg2 = 4;
            Bundle te = new Bundle();
            te.putLong(SendFileInfoActivity.UPDATE_ACCEPT_LEN, i * 10);
            message.setData(te);
            handler.sendMessage(message);
            try {
                sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Message message = new Message();
        message.what = SendFileInfoActivity.END;
        message.arg1 = pos;
        message.arg2 = 4;
        handler.sendMessage(message);
    }
}
