package com.example.acbingo.myfinaltest10.activitys;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.acbingo.myfinaltest10.FileManagerService.FileManagerMainActivity;
import com.example.acbingo.myfinaltest10.MyWifiManager.MyWifiManager;
import com.example.acbingo.myfinaltest10.R;
import com.jaeger.library.StatusBarUtil;

import java.util.List;

/**
 * Created by tfuty on 2016-06-12.
 */
public class ReceiveFileEndActivity extends AppCompatActivity {

    WifiManager wifiManager;

    TextView textView;
    Button check_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file_end);

        StatusBarUtil.setTransparent(this);

        Button button = (Button) findViewById(R.id.exit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        textView = (TextView) findViewById(R.id.textview);
        textView.setText("所有文件保存在了:"
                +ReceiveFileService.getFileSavePath());

        check_btn = (Button) findViewById(R.id.check_btn);

        check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent te = new Intent(getApplicationContext() , FileManagerMainActivity.class);
                te.putExtra("path",ReceiveFileService.getFileSavePath());
                startActivity(te);
                finish();
            }
        });

        deleteWifiinfo();

        //MyWifiManager.getMyWifiManagerInstance(getBaseContext()).disconnectAll();
    }

    private void deleteWifiinfo(){
        if (!wifiManager.isWifiEnabled()) return;
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.contains(MyWifiManager.wifiHotPrefix)){
                wifiManager.removeNetwork(config.networkId);
                Log.d("WifiConfig", config.SSID+"is delete");
            }

        }
    }
}
