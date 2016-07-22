package com.example.acbingo.myfinaltest10.VoiceSystem;

import android.content.Context;
import android.os.Handler;

import com.libra.sinvoice.Common;
import com.libra.sinvoice.SinVoicePlayer;

import java.io.UnsupportedEncodingException;

/**
 * Created by tfuty on 2016-05-24.
 */
public class SendVoice implements SinVoicePlayer.Listener {
    //  装载库文件
    static {
        System.loadLibrary("sinvoice");
    }
    private final static int[] TOKENS = { 32, 32, 32, 32, 32, 32 };
    private final static int TOKEN_LEN = TOKENS.length;
    private SinVoicePlayer mSinVoicePlayer;
    private Handler handler;


    //指定handler,当传完后会通过handler向上层传递结束信息
    public SendVoice(Context context, Handler handler){
        mSinVoicePlayer = new SinVoicePlayer();
        mSinVoicePlayer.init(context);
        mSinVoicePlayer.setListener(this);
        this.handler = handler;
    }

    public void send(String s){
        try{
            byte[] strs = s.getBytes("UTF8");
            if (strs != null){
                int len = strs.length;
                int[] tokens = new int[len];
                int maxEncoderIndex = mSinVoicePlayer.getMaxEncoderIndex();
                //Log.d(TAG,""+maxEncoderIndex);
                String encoderText = s;
                for ( int i = 0; i < len; ++i ) {
                    if ( maxEncoderIndex < 255 ) {
                        tokens[i] = Common.DEFAULT_CODE_BOOK.indexOf(encoderText.charAt(i));
                    } else {
                        tokens[i] = strs[i];
                    }
                }
                mSinVoicePlayer.play(tokens, len, false, 2000);
            }else {
                mSinVoicePlayer.play(TOKENS, TOKEN_LEN, false, 2000);
            }
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }



    public void stop(){
        mSinVoicePlayer.stop();
    }

    public void uninit(){
        mSinVoicePlayer.uninit();
    }

    @Override
    public void onSinVoicePlayStart() {
        handler.sendEmptyMessage(VoiceContent.MSG_PLAY_START);
    }

    @Override
    public void onSinVoicePlayEnd() {
        handler.sendEmptyMessage(VoiceContent.MSG_PLAY_END);
    }

    @Override
    public void onSinToken(int[] tokens) {

    }
}
