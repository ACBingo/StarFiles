package com.example.acbingo.myfinaltest10.WanTransmission;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.itangqi.waveloadingview.WaveLoadingView;
import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by tfuty on 2016-06-10.
 */
public class UpActivity extends AppCompatActivity {
    private  final static int RequestCode = 1;
    Button choosefile_button;
    WaveLoadingView waveLoadingView;
    BadgeView badgeView;

    public static final int Server_is_read_to_receive = 10;
    public static final int Update_progress = 11;
    public static final int Update_END = 12;
    public static final int FileCode = 9;
    public static final int Server_Fail = 8;
    public static final int Update_begin = 7;

    int flag = 0;

    private static File theChooseFile;

    Client_send_service client_send_service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        StatusBarUtil.setTransparent(this);

        choosefile_button = (Button) findViewById(R.id.choosefile);
        waveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        badgeView = (BadgeView) findViewById(R.id.badge_view);

        client_send_service = new Client_send_service(getBaseContext(),handler);

        waveLoadingView.setCenterTitle("等待中");

        choosefile_button.setText("等待连接");

        choosefile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choosefile_button.getText().toString().equals("返回")){

                    finish();
                }
                else if (choosefile_button.getText().equals("选择文件")){

                    Toast.makeText(getBaseContext(),"目前版本只支持发送一个文件哦，选择多个文件的话默认只发送第一个文件~"
                            ,Toast.LENGTH_SHORT).show();

                    Intent te = new Intent(getBaseContext(),ChooseFileActivity.class);
                    startActivityForResult(te , ChooseFileActivity.CHOOSEFILEACTIVITYRESULT);
                    choosefile_button.setText("发送");
                    choosefile_button.setTag(1);
                }else if (choosefile_button.getText().equals("发送")){
                    if (FileChoose.getFileChooseCount()==0){
                        Toast.makeText(getBaseContext(),"您还没有选择文件哦",Toast.LENGTH_SHORT).show();
                        choosefile_button.setText("选择文件");
                    }else{

                        theChooseFile = FileChoose.getFileChooseArrayList().get(0);
                        client_send_service.sendFile(theChooseFile);

                        choosefile_button.setText("正在发送中");

                    }
                }

            }
        });


    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case Server_is_read_to_receive:

                    badgeView.setValue("连接服务器成功");
                    choosefile_button.setText("选择文件");

                    break;

                case Update_begin:
                    waveLoadingView.setTopTitle("正在上传"+theChooseFile.getName().toString());
                    double length =(double) theChooseFile.length()/1024/1024;
                    length = (double)(Math.round(length*100)/100.0);
                    waveLoadingView.setBottomTitle("文件大小"+length+"MB");
                    break;
                case Update_progress:
                    int progress = msg.arg1;
                    waveLoadingView.setCenterTitle(progress+"%");
                    waveLoadingView.setProgressValue(progress);
                    break;
                case Update_END:
                    waveLoadingView.setTopTitle(theChooseFile.getName()+" 上传完成");
                    waveLoadingView.setCenterTitle(100+"%");
                    waveLoadingView.setProgressValue(100);
                    choosefile_button.setText("返回");
                    break;
                case FileCode:
                    String fileCode = (String) msg.obj;
                    badgeView.setValue("FileCode is : "+fileCode);
                    Toast.makeText(getBaseContext(),"以后你可以在任何时刻任何一台" +
                            "已经联网的安卓或者PC设备上通过FileCode下载" +
                            "该文件,请妥善保管"
                            ,Toast.LENGTH_LONG).show();
                    break;
                case Server_Fail:
                    //Todo 做成一个对话框
                    Toast.makeText(getBaseContext(),"呀，失败了~\n" +
                                    "1.请确认本设备是否可以正常访问网络。\n" +
                                    "2.按返回键返回到上一个界面，然后重新进入到本界面\n"+
                                    "3.联系开发者确认服务器是否正常。\n"
                            ,Toast.LENGTH_LONG).show();
                    flag = 0;
                    //waveLoadingView.setCenterTitle("failed");
                    badgeView.setValue("连接服务器失败");
                    choosefile_button.setText("返回");
            }
        }
    };
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
