package com.example.acbingo.myfinaltest10.VoiceSystem;

import android.content.Context;
import android.os.Handler;

import com.libra.sinvoice.SinVoiceRecognition;

/**
 * Created by tfuty on 2016-05-05.
 */
public class ReceiveVoice implements
        SinVoiceRecognition.Listener {

    static {
        System.loadLibrary("sinvoice");
        //LogHelper.d(TAG, "sinvoice jnicall loadlibrary");
    }

    private final static int[] TOKENS = { 32, 32, 32, 32, 32, 32 };
    private final static String TOKENS_str = "Beeba20141";
    private final static int TOKEN_LEN = TOKENS.length;

    private char mRecgs[] = new char[100];
    private int mRecgCount;
    private SinVoiceRecognition mRecognition;

    private Context context;

    private Handler handler;

/*
    private static ReceiveVoice receiveVoiceInstance = null;

    public static ReceiveVoice getReceiveVoice(Context context,Handler handler){
        if (receiveVoiceInstance!=null) return receiveVoiceInstance;
        receiveVoiceInstance = new ReceiveVoice(context,handler);
        return receiveVoiceInstance;
    }
*/
    public ReceiveVoice(Context context){
        mRecognition = new SinVoiceRecognition();
        mRecognition.init(context);
        mRecognition.setListener(this);

        handler = new Handler();
    }
    //如果指定handler就是当传完后会通过handler向上层传递结束信息
    public ReceiveVoice(Context context, Handler handler){
        mRecognition = new SinVoiceRecognition();
        mRecognition.init(context);
        mRecognition.setListener(this);

        this.handler = handler;
    }

    public void start(){
        mRecognition.start(TOKEN_LEN, false);
    }

    public void stop(){
        mRecognition.stop();
    }

    public void uninit(){
        mRecognition.uninit();
    }

    @Override
    public void onSinVoiceRecognitionStart() {
        //开始听
        handler.sendEmptyMessage(VoiceContent.MSG_RECG_START);
    }

    @Override
    public void onSinVoiceRecognition(char ch) {
        //一个字符一个字符的收过来
        handler.sendMessage(handler.obtainMessage(VoiceContent.MSG_SET_RECG_TEXT, ch, 0));
    }

    @Override
    public void onSinVoiceRecognitionEnd(int result) {
        //已经接受完消息，将消息传到ui线程
        handler.sendMessage(handler.obtainMessage(VoiceContent.MSG_RECG_END,result,0));
    }

}
