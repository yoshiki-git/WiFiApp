package com.example.wifiapp

import android.app.ActivityManager
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log

class GetDeviceData(context: Context) {

    private val TAG = "GetDeviceData"
    val wifiManager :WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    //Wifimanager.WifiInfoの方
    lateinit var conWifi_Info: WifiInfo
    var  mWifiList = mutableListOf<Wifi_Info>()

    fun getWifiData() :MutableList<Wifi_Info> {
        //アプリ起動時に自動でWifiスキャンをする
        val scanResult=wifiManager.startScan()
        val wifiList : List<Wifi_Info>

        wifiList = if (scanResult){
            Log.d(TAG,"wifi scan 成功")
            scanSuccess()
        }else{
            Log.d(TAG,"wifi scan 失敗")
            firstScanFailure()
        }
        return wifiList
    }

    fun scanSuccess():MutableList<Wifi_Info>{
        mWifiList.clear()
        Log.d(TAG,"mWifiList clear")
        val results:List<ScanResult> = wifiManager.scanResults
        conWifi_Info = wifiManager.connectionInfo
        Log.d(TAG,"results size:${results.size}")


        //BSSIDにより、ScanResultの中から、接続しているWifiを特定する
        val bssid_connected:String
        //何も接続していない場合
        if (conWifi_Info.bssid ==null){
            bssid_connected = "null"
            Log.d(TAG,"Do not connect")
            Log.d(TAG,"Connected: ${conWifi_Info.ssid}")
            Log.d(TAG,"linkspeed:${conWifi_Info.linkSpeed}")
        }else{
            //接続していた場合、BSSIDと回線速度をログで表示
            bssid_connected = conWifi_Info.bssid
            Log.d(TAG,"Connected: ${conWifi_Info.ssid}")
            Log.d(TAG,"linkspeed:${conWifi_Info.linkSpeed}")
        }

        for (i in results){
            if (bssid_connected ==i.BSSID){
                mWifiList.add(Wifi_Info(i.SSID,i.BSSID,i.level,i.frequency,R.drawable.wifi_con))
                Log.d(TAG,"mWifiList add")

            }else{
                mWifiList.add(Wifi_Info(i.SSID,i.BSSID,i.level,i.frequency,R.drawable.wifi))
                Log.d(TAG,"mWifiList add")
            }
        }
        //   Log.d(TAG,"mWifiList:$mWifiList")
        return mWifiList
    }


    fun firstScanFailure():MutableList<Wifi_Info>{
        //テキトーなデータ
        val df_wifiInfo = Wifi_Info("null","null",0,0,R.drawable.wifi_con)
        mWifiList .add(df_wifiInfo)
        //    Log.d(TAG,"mWifiList:$mWifiList")
        return mWifiList
    }

}