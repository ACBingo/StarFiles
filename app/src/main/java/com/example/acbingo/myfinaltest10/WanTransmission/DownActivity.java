package com.example.acbingo.myfinaltest10.WanTransmission;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.acbingo.myfinaltest10.FileManagerService.FileManagerMainActivity;
import com.example.acbingo.myfinaltest10.R;
import com.example.acbingo.myfinaltest10.activitys.ReceiveFileService;
import com.jaeger.library.StatusBarUtil;

import me.itangqi.waveloadingview.WaveLoadingView;
import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by tfuty on 2016-06-12.
 */
public class DownActivity extends AppCompatActivity {

    public static final int File_Code_is_right = 10;
    public static final int File_Code_is_wrong = 11;
    public static final int Update_Progress = 12;
    public static final int Update_End = 13;
    public static final int Failed = 14;
    public static final int File_Name = 15;
    public static final int File_Length = 16;
    public static final int Server_is_ready = 0;

    Client_receive_service client_receive_service;

    Button download_button;
    EditText editText;
    WaveLoadingView waveLoadingView;
    BadgeView badgeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        StatusBarUtil.setTransparent(this);

        client_receive_service = new Client_receive_service(handler);
        //client_receive_service.init();

        download_button = (Button) findViewById(R.id.download_button);
        editText = (EditText) findViewById(R.id.edittext);

        waveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        badgeView = (BadgeView) findViewById(R.id.badge_view);



        download_button.setClickable(false);
        download_button.setText("等待连接服务器");

        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (download_button.getText().equals("返回")){
                    finish();
                }else if (download_button.getText().equals("下载")){
                    String tmp = editText.getText().toString();
                    if (tmp.length()!=6){
                        Toast.makeText(getBaseContext(),"请输出正确6位文件标识码",Toast.LENGTH_SHORT).show();
                    }else{
                        client_receive_service.begin(tmp);
                    }
                }else if (download_button.getText().equals("查看文件")){
                    Intent te = new Intent(getApplicationContext() , FileManagerMainActivity.class);
                    te.putExtra("path", ReceiveFileService.getFileSavePath());
                    startActivity(te);
                    DownActivity.this.finish();
                    //Todo
                }

            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case File_Code_is_right:
                    badgeView.setValue("正在下载");
                    download_button.setText("正在下载");
                    download_button.setClickable(false);
                    client_receive_service.receiveFile();
                    break;
                case File_Code_is_wrong:
                    badgeView.setValue("标识码错误,请重新输入");
                    Toast.makeText(getBaseContext(),"该标识码对应的文件不存在,请重新输入",Toast.LENGTH_SHORT).show();
                    /*String tmp = editText.getText().toString();
                    if (tmp.length()!=6){
                        Toast.makeText(getBaseContext(),"请输出正确6位文件标识码",Toast.LENGTH_SHORT).show();
                    }else{
                        client_receive_service.begin(tmp);
                    }*/
                    break;

                case Update_Progress:
                    int progress = msg.arg1;
                    waveLoadingView.setCenterTitle(progress+"%");
                    waveLoadingView.setProgressValue(progress);
                    break;
                case Update_End:
                    badgeView.setValue("下载完成");
                    download_button.setText("返回");
                    download_button.setClickable(true);
                    waveLoadingView.setCenterTitle(100+"%");
                    waveLoadingView.setProgressValue(100);

                    download_button.setText("查看文件");
                    //Todo 查看文件
                    //Toast.makeText()
                    break;
                case Failed:
                    waveLoadingView.setCenterTitle("Failed");
                    badgeView.setValue("无法连接到服务器");
                    Toast.makeText(getBaseContext(),"呀，失败了!\n" +
                            "1.请检查网络是否正常." +
                            "\n2.联系开发者确认服务器是否正常",Toast.LENGTH_SHORT).show();
                    download_button.setText("返回");
                    break;

                case File_Name:
                    waveLoadingView.setTopTitle("正在下载:"+(String)msg.obj);
                    break;

                case File_Length:
                    long filelength = (long)msg.obj;
                    double length =(double)filelength/1024/1024;

                    length = (double)(Math.round(length*100)/100.0);
                    waveLoadingView.setBottomTitle("文件大小"+length+"MB");
                    break;

                case Server_is_ready:
                    badgeView.setValue("已经连接到服务器");
                    download_button.setText("下载");
                    break;

            }
        }
    };
}
