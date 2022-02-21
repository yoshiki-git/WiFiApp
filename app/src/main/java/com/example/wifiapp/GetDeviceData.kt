package com.example.wifiapp

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import kotlin.coroutines.coroutineContext

class GetDeviceData(context: Context) {

    private val TAG = "GetDeviceData"
    //WifiManagerの定義
    val wifiManager :WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    //Wifimanager.WifiInfoの方
    lateinit var conWifi_Info: WifiInfo
    var  mWifiList = mutableListOf<Wifi_Info>()

/*
    fun getBLEData(){

        var scanFilter: ScanFilter = ScanFilter.Builder()
            .build()
        var scanFilterList:ArrayList<ScanFilter> = ArrayList()
        scanFilterList.add(scanFilter)

        var scanSettings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        //スキャンで見つかったデバイスが飛んでくる
        val scanCallback: ScanCallback =
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
                super.onScanResult(callbackType, result)
                if (result != null) {
                    Log.d(TAG, "BT_Name:"+result.device.name)
                    Log.d(TAG, "BT_Address:"+result.device.address)
            //        Log.d(TAG, "BT_TxPower:"+result.txPower)
            //        Log.d(TAG, "BT_RSSI:"+result.rssi)
            //        Log.d(TAG, "BT_Type:"+result.device.type)
            //        Log.d(TAG, "BT_Connectable:"+result.isConnectable)
            //        Log.d(TAG, "BT_UUID:"+result.device.uuids)
            //        Log.d(TAG,"BT_ConType:"+ result.device.bluetoothClass)

                }
            }
        }

        bluetoothLeScanner.startScan(scanFilterList,scanSettings,scanCallback)
    }


 */


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

    fun scanJudge() :Boolean{
        return wifiManager.startScan()
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

    //Bluetoothの接続状態を返す
    fun getBondState(state: Int):String{
        var str = ""
        str = when(state){
            BluetoothDevice.BOND_BONDED ->
                "接続履歴あり"
            BluetoothDevice.BOND_BONDING ->
                "接続中"
            BluetoothDevice.BOND_NONE ->
                "接続履歴なし"
            else ->
                "ERROR"
        }
        return str
    }

    //Bluetoothスキャンできた端末の種類を返す
    fun getDeviceType(state: Int):String{
        var str = ""
        //https://developer.android.com/reference/android/bluetooth/BluetoothClass.Device.html
        //https://developer.android.com/reference/android/bluetooth/BluetoothClass.Device.Major
        str = when(state){
            BluetoothClass.Device.PHONE_SMART ->
                "PHONE_SMART"
            BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET ->
                "AUDIO_VIDEO_WEARABLE_HEADSET"
            BluetoothClass.Device.Major.UNCATEGORIZED ->
                "UNCATEGORIZED"
            else ->
                "OTHER:$state"
        }
        return str
    }

    fun getDeviceConnectType(state: Int):String{
        var str = ""
        //https://developer.android.com/reference/android/bluetooth/BluetoothDevice#DEVICE_TYPE_CLASSIC

        str = when(state){
            BluetoothDevice.DEVICE_TYPE_CLASSIC ->
                "Classic -BR/EDR devices"
            BluetoothDevice.DEVICE_TYPE_DUAL ->
                "Dual Mode -BR/EDR/LE"
            BluetoothDevice.DEVICE_TYPE_LE ->
                "Low Energy -LE-only"
            else ->
                "ERROR"
        }
        return str

    }

    fun mySerCheck(context: Context):Boolean{
        //ログ保存サービスが起動中かをチェック
        val am:ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val serlist = am.getRunningServices(Integer.MAX_VALUE)
        for (info in serlist) {
            if (MyService::class.java.canonicalName.equals(info.service.getClassName())) {
                Log.d(TAG,"ser running")
                return true
            }
        }
        Log.d(TAG,"ser isn't ruunning")
        return false
    }



}