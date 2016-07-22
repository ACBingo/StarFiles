package com.example.acbingo.myfinaltest10.MyWifiManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * Created by tfuty on 2016-05-12.
 */
public class MyWifiManager {
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;

    public static String wifiHotPrefix = "BingoHot";

    private static String  SERVERIP = null;
    private static int SERVERPORT = 5418;

    /*生成一个随机的ssid*/
    private static String SSID = null;
    public static String getWifiHotSSID(){
        if(SSID!=null) return SSID;
        Random random = new Random();
        int t=0;
        //保证生成一个4位的随机数
        while (t<1000)
            t = random.nextInt(10000);

        SSID = wifiHotPrefix+((Object)t).toString();
        return  SSID;
    }
    /*生成一个随机的password*/
    private static String PASSWORD = null;
    public static String getWifiHotPassword(){
        if (PASSWORD!=null) return PASSWORD;
        Random random = new Random();
        PASSWORD = "";
        for (int i=0;i<8;i++){
            PASSWORD+=((Object)random.nextInt(10)).toString();
        }
        return PASSWORD;
    }


    private Context context;
    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetworkInfo;

    private WifiManager wifiManager;

    private MyWifiManager(Context context){
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

    }
    private static MyWifiManager myWifiManagerInstance=null;
    public static MyWifiManager getMyWifiManagerInstance(Context context){
        if (myWifiManagerInstance!=null) return myWifiManagerInstance;
        myWifiManagerInstance =new MyWifiManager(context);
        return myWifiManagerInstance;
    }

    /*
    * 断开当前的所有连接(wifi,aphot,3g/4g)
    */
    public void disconnectAll(){
        if (wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(false);
        if (getWifiApState()==WIFI_AP_STATE_ENABLED){
            setWifiApEnabled(false);
        }
        //Todo 移动数据的关闭
        /*if (activeNetworkInfo.getType()==ConnectivityManager.TYPE_MOBILE){
            Log.d("mydebug","yes");
            setGprsStatus(context,false);
        }*/
    }
    /*获取ap状态*/
    public int getWifiApState() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(wifiManager);
            return i;
        } catch (Exception e) {
            return -1;
        }
    }
    //通过反射开启/关闭ap
    public boolean setWifiApEnabled(boolean enabled){
        try{
            Log.d("mydebug","A");
            WifiConfiguration apConfig = new WifiConfiguration();
            Log.d("mydebug","b");
            apConfig.SSID = getWifiHotSSID();
            Log.d("mydebug",getWifiHotSSID());
            apConfig.preSharedKey = getWifiHotPassword();
            Log.d("mydebug",getWifiHotPassword());
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE
            );
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e){
            //Todo 创建热点失败的相关信息
            return false;
        }
    }
    /*检测移动数据打开/关闭*/
    private boolean getMobileDataEnabled() throws Exception {
        ConnectivityManager mcm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class ownerClass = mcm.getClass();
        Method method = ownerClass.getMethod("getMobileDataEnabled");
        return (Boolean)method.invoke(mcm);
    }
    /*打开/关闭移动数据*/
    public static void setGprsStatus(Context context,boolean isEnable){
        ConnectivityManager mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class<?> cmClass = mConnectivityManager.getClass();
        Class<?>[] argClasses = new Class[1];
        argClasses[0] = boolean.class;

        // 反射ConnectivityManager中hide的方法setMobileDataEnabled，可以开启和关闭GPRS网络
        Method method;
        try {
            method = cmClass.getMethod("setMobileDataEnabled", argClasses);
            method.invoke(mConnectivityManager, isEnable);
        } catch (NoSuchMethodException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }
    /*开启wifiap*/
    public void wifiHotCreate(){
        Log.d("mydebug","yes");
        setWifiApEnabled(true);
        Log.d("mydebug","yes2");
    }

    public Boolean toConnectWifiHot(Context context,String _ssid,String _password){

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        WifiConfiguration wifiConfiguration = setWiiParams(_ssid,_password);
        int wcgID = wifiManager.addNetwork(wifiConfiguration);
        boolean flag = wifiManager.enableNetwork(wcgID,true);
        //wifiManager.saveConfiguration();
        //wifiManager.reconnect();
        return flag;
    }
    public static WifiConfiguration setWiiParams(String _SSID,String _PASSWORD){
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID="\""+_SSID+"\"";
        apConfig.preSharedKey="\""+_PASSWORD+"\"";
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        return apConfig;
    }


    public static void setSERVERIP(String s){
        SERVERIP = s;
    }
    public static int getSERVERPORT(){
        return SERVERPORT;
    }
    public static String getSERVERIP(){
        if (SERVERIP!=null) return SERVERIP;
        SERVERIP = "0.0.0.0";
        return SERVERIP;
    }
    public static String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }


}
