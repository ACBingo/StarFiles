package com.example.acbingo.myfinaltest10.PCTransmission;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.acbingo.myfinaltest10.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jaeger.library.StatusBarUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by tfuty on 2016-06-13.
 */
public class PCMainActivity extends AppCompatActivity {

    Button send_btn;
    Button receive_btn;

    String SERVERIP ;

    public static Socket socket;

    int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_main);

        StatusBarUtil.setTransparent(this);

        send_btn = (Button) findViewById(R.id.send_btn);
        receive_btn = (Button) findViewById(R.id.receive_btn);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=1;
                IntentIntegrator integrator = new IntentIntegrator(PCMainActivity.this);
                integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
            }
        });

        receive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=2;
                IntentIntegrator integrator = new IntentIntegrator(PCMainActivity.this);
                integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                //Log.d(TAG, "Cancelled");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Log.d(TAG, "Scanned: " + result.getContents());
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                //textView.setText(result.getContents());
                SERVERIP = result.getContents();


                if (flag==1){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket = new Socket(SERVERIP,12345);

                                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                                dos.writeBoolean(true);
                                dos.flush();

                                Intent intent1 = new Intent(getBaseContext(),FileSendToPCActivity.class);
                                intent1.putExtra("serverip",SERVERIP);
                                startActivity(intent1);

                                finish();
                            } catch (IOException e) {
                                e.printStackTrace();
                                handler.sendEmptyMessage(0);
                            }
                        }
                    }).start();


                }else if (flag==2){

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket = new Socket(SERVERIP,12345);

                                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                                dos.writeBoolean(false);
                                dos.flush();

                                Intent intent1 = new Intent(getBaseContext(),FileReceiveFromPCActivity.class);
                                intent1.putExtra("serverip",SERVERIP);
                                startActivity(intent1);
                                finish();

                            } catch (IOException e) {
                                e.printStackTrace();
                                handler.sendEmptyMessage(0);
                            }
                        }
                    }).start();


                }
            }
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    Toast.makeText(getBaseContext(),"连接失败！\n请检查网络连接并保证与PC端处于同一局域网下",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
