package com.example.acbingo.myfinaltest10.activitys;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.acbingo.myfinaltest10.MyWifiManager.MyWifiManager;
import com.example.acbingo.myfinaltest10.R;
import com.jaeger.library.StatusBarUtil;

import java.util.List;

/**
 * Created by tfuty on 2016-06-10.
 */
public class SendFileEndActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file_end);

        StatusBarUtil.setTransparent(this);

        MyWifiManager.getMyWifiManagerInstance(getBaseContext()).disconnectAll();

        //deleteWifiinfo();
        //Todo 删除冗余的wifi热点信息

        Button button = (Button) findViewById(R.id.exit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

    }

    private void deleteWifiinfo(){
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.contains(MyWifiManager.wifiHotPrefix)){
                wifiManager.removeNetwork(config.networkId);
                Log.d("WifiConfig", config.SSID+"is delete");
            }

        }
    }
}
