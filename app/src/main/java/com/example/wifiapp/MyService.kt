package com.example.wifiapp

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import java.io.File

class MyService : Service() {
    val TAG = "MyService.kt"

    private lateinit var context: Context
    private lateinit var getLogData: GetLogData
    private lateinit var file :File
    private val getTimeData=GetTimeData()
    lateinit var wifiManager: WifiManager
    //Wifimanager.WifiInfoの方
    lateinit var wifi_info:WifiInfo

    private var checkBoxStatus : Boolean = false
    private var isGetWifiLog:Boolean = true
    private var isGetBTLog: Boolean = false
    private var isGetBLELog: Boolean = false

   // lateinit var mWifiReceiver: WifiScanReceiver
  //  private var btLogReceiver: BroadcastReceiver? = null


    override fun onCreate() {
        super.onCreate()

        super.onCreate()
        //context取得
        context=applicationContext
        //getLogData生成
        getLogData= GetLogData(context)

        //チェックボックスの設定値呼び出し
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        checkBoxStatus = pref.getBoolean("checkStatus",false)
        //Wifiを取るかの設定
        isGetWifiLog = pref.getBoolean(
            context.resources.getString(R.string.settings_wifi_key),
            context.resources.getBoolean(R.bool.settings_wifi_default)
        )
        //Bluetoothを取るかの設定
        isGetBTLog = pref.getBoolean(
            context.resources.getString(R.string.settings_bt_key),
            context.resources.getBoolean(R.bool.settings_bt_default)
        )
        //BLEを取るかの設定
        isGetBLELog = pref.getBoolean(
            context.resources.getString(R.string.settings_ble_key),
            context.resources.getBoolean(R.bool.settings_ble_default)
        )

        //ファイル名を現在時刻に設定する
        val start_time=getTimeData.getFileName()
        //拡張子をつける
        val fileName=start_time+"_Log"+".csv"
        file=getLogData.getFileStatus(fileName)

        wifiManager=context.getSystemService(Context.WIFI_SERVICE) as WifiManager
     //   wifi_info = wifiManager.connectionInfo

        //カラムの作成
        val columns = mutableListOf<String>()
        columns.add("timestamp")
        columns.add("SSID")
        columns.add("BSSID")
        columns.add("Connected")
        columns.add("IP Address")
        columns.add("Link Speed")
        columns.add("capabilities")
        columns.add("level")
        columns.add("frequency")
        columns.add("ChannelBandWidth")
        //ここからBluetooth情報
        columns.add("Bt_Name")
        columns.add("Bt_HWAddress")
        columns.add("Bt_RSSI")
        columns.add("Bt_BOND_STATUS")
        columns.add("Bt_Type")
        columns.add("Bt_Tech")
        getLogData.getColumn(file,columns)

        /*
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        mWifiReceiver = WifiScanReceiver()
        context.registerReceiver(mWifiReceiver, intentFilter)
         */
    }



    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        super.onDestroy()

        //アラームの終了
        stopAlarmService()

      //  context.unregisterReceiver(mWifiReceiver)
        //クラッシュするのでコメントアウト
      //  context.unregisterReceiver(BtLogReceiver)
        //サービス終了
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG,"onStartCommand")

        val requestCode = intent!!.getIntExtra("REQUEST_CODE", 0)
        //    val context = applicationContext
        val channelId = "RepeatWifiScan"
        val title = context.getString(R.string.app_name)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }else{
            PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        // Notification　Channel 設定
        //IMPORTANCE_LOWにして通知音を消している
        val channel = NotificationChannel(
            channelId, title, NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
        val notification = Notification.Builder(context, channelId)
            .setContentTitle(title) // android標準アイコンから
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentText("Wifiログ取得中")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .build()

        // startForeground 第一引数のidで通知を識別
        startForeground(9999, notification)

        //ログの時刻指定のオンオフで処理を切り替える

        //オンの時
        if (checkBoxStatus){
            Log.d(TAG,"CheckBox is true")
            //9時を過ぎてるかをチェック
            val isBefore9 = getTimeData.compareTime(9,0)
            val isBefore18 = getTimeData.compareTime(18,0)

            if (!isBefore9 && isBefore18){
                Log.d(TAG,"間だよ")
                val success = wifiManager.startScan()

                if (success) {
                    // scan failure handling
                    scanSuccess()
                }else{
                    scanFailure()
                }
            }else{
                Log.d(TAG,"間じゃないよ")
            }
        }
        //オフの時
        else{
            Log.d(TAG,"CheckBox is false")

            //Wifiを取るかの設定を参照
            if (isGetWifiLog){
                val success = wifiManager.startScan()

                if (success) {
                    // scan failure handling
                    scanSuccess()
                }else{
                    scanFailure()
                }
            }
            //Bluetoothを取るかの設定を参照
            if(isGetBTLog){
                //Bluetooth情報の取得
                val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

                // Register for broadcasts when a device is discovered.
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)


                context.registerReceiver(BtLogReceiver, filter)




                bluetoothAdapter?.startDiscovery()
                Log.d(TAG,"register receiver")
            }

        }

        //毎回Alarmの設定
        setNextAlarmService(context)

        //return START_NOT_STICKY;
        //return START_STICKY;
        return START_REDELIVER_INTENT
    }

    inner class WifiScanReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG,"SCAN_RESULT_AVAILABLE_ACTION is Received")
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }



    // 次のアラームの設定
    private fun setNextAlarmService(context: Context) {

        //チェックボックスの設定値呼び出し
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        checkBoxStatus = pref.getBoolean("checkStatus",false)
        // 30s毎のアラーム設定
        val repeatPeriod = (30 * 1000).toLong()
        val intent = Intent(context, MyService::class.java)
    //    val startMillis = SystemClock.elapsedRealtime() + repeatPeriod
        val startMillis = System.currentTimeMillis() + repeatPeriod
        val pendingIntent = PendingIntent.getService(context, 0, intent, FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            startMillis, pendingIntent
        )
    }

    private fun stopAlarmService() {
        val intent = Intent(context, MyService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent, FLAG_IMMUTABLE)

        // アラームを解除する
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    //スキャン成功時にWifi情報を取得し記録する
    fun scanSuccess() {
        Log.d(TAG,"wifi scan succeeded")

        //スキャン結果の受け取り
        val results:List<ScanResult> = wifiManager.scanResults
     //   Log.d(TAG,results.toString())

        //スキャン成功時にトーストで通知
        Toast.makeText(context,"wifi scan 成功", Toast.LENGTH_SHORT).show()
        //ログデータ用に時刻を取得
        val time=getTimeData.getNowTime()
        //現在接続しているWifiの情報を取得する
        wifi_info = wifiManager.connectionInfo
        //BSSIDにより、ScanResultの中から、接続しているWifiを特定する
        val bssid_connected:String
        //何も接続していない場合
        if (wifi_info.bssid ==null){
            bssid_connected = "null"
            Log.d(TAG,"Do not connect")
            Log.d(TAG,"Connected: ${wifi_info.ssid}")
            Log.d(TAG,"linkspeed:${wifi_info.linkSpeed}")
        }else{
            //接続していた場合、BSSIDと回線速度をログで表示
            bssid_connected = wifi_info.bssid
            Log.d(TAG,"Connected: ${wifi_info.ssid}")
            Log.d(TAG,"linkspeed:${wifi_info.linkSpeed}")
        }

        //取得結果はBSSID毎にScanResultの配列となっている。この中から必要な情報をログに保存する
        for (i in results){
            val stringBuilder = StringBuilder()
            stringBuilder.append(time)
                .append(",")
                .append(i.timestamp)
                .append(",")
                .append(i.SSID)
                .append(",")
                .append(i.BSSID)
                .append(",")
            //接続しているWifiの場合はIPアドレスと回線速度が取得可能なので取得する
            //if分によりBSSIDを照合
            if (bssid_connected ==i.BSSID){
                val ip_addr_i:Int = wifi_info.ipAddress
                val ip_addr =
                    (ip_addr_i shr 0 and 0xFF).toString() + "." + (ip_addr_i shr 8 and 0xFF) + "." + (ip_addr_i shr 16 and 0xFF) + "." + (ip_addr_i shr 24 and 0xFF)
                stringBuilder.append("true")
                    .append(",")
                    .append(ip_addr)
                    .append(",")
                    .append(wifi_info.linkSpeed)
                //API29以降はrxlinkspeed(下り回線速度）txlinkspeed(上り回線速度）の取得が可能。
                //該当端末が手元にないので今回は割愛
           //     Log.d(TAG,"rxlinkspeed:${wifi_info.rxLinkSpeedMbps}")
            }else{
                stringBuilder.append("false")
                    .append(",")
                    .append("N/A")
                    .append(",")
                    .append("N/A")
            }

            stringBuilder.append(",")
                .append(i.capabilities)
                .append(",")
                .append(i.level)
                .append(",")
                .append(i.frequency)
                .append(",")
                .append(channelWidthToString(i.channelWidth))
                .append(",,,,,,")
                .append("\n")
            getLogData.getLog(file,stringBuilder.toString())

        }

    //    Log.d(TAG,"scan result:${results[0]}")
    }

    //スキャン失敗時の処理
    fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        //Doze中、画面消灯時にはWifiスキャンは失敗する
        //位置情報オフでも失敗する
        Toast.makeText(context,"wifi scan 失敗", Toast.LENGTH_SHORT).show()
        val stringBuilder = StringBuilder()
        stringBuilder.append(getTimeData.getNowTime())
                     .append(",")
                     .append("wi-fi scan failed")
                     .append("\n")
        getLogData.getLog(file,stringBuilder.toString())
        Log.d(TAG,"wi-fi scan failed")

    }

    fun channelWidthToString(i:Int) =
        when(i){
            0 -> "20"
            1 -> "40"
            2 -> "80"
            3 -> "160"
            4 -> "80+80"
            else -> "Unknown"
        }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val BtLogReceiver:BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

            val sb = StringBuilder()

            val time = getTimeData.getNowTime()

            sb.append(time)
                .append(",,,,,,,,,,,")

            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    Log.d(TAG,"ACTION_FOUND is received")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    val getDeviceData:GetDeviceData = GetDeviceData(context)

                    if (device!=null){
                        val deviceName = if (device.name != null){
                            device.name
                        }else{
                            "null"
                        }
                        val deviceHardwareAddress = device.address // MAC address
                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        //接続状態だが、なぜか接続中であるBONDINGを返さない
                        val bondState = device.bondState.let { getDeviceData.getBondState(it) }
                        //  val deviceType = device.bluetoothClass?.deviceClass?.let { getDeviceData.getDeviceType(it) }
                        val deviceType = if(device.bluetoothClass != null){
                            device.bluetoothClass.deviceClass.let { getDeviceData.getDeviceType(it) }
                        }else{
                            "null"
                        }
                        val deviceConType = device.type.let { getDeviceData.getDeviceConnectType(it) }

                        sb.append(deviceName)
                            .append(",")
                            .append(deviceHardwareAddress)
                            .append(",")
                            .append(rssi)
                            .append(",")
                            .append(bondState)
                            .append(",")
                            .append(deviceType)
                            .append(",")
                            .append(deviceConType)
                            .append(",")
                            .append("\n")

                        getLogData.getLog(file,sb.toString())
                        Log.d(TAG,"Name:$deviceName")
                        Log.d(TAG,"hardwareAddress:$deviceHardwareAddress")
                        //      Log.d(TAG,"BT:bondState:${device?.bondState}")
                        //      Log.d(TAG,"BT:type:${device?.type}")
                        Log.d(TAG,"BT:deviceClass:${device?.bluetoothClass?.deviceClass}")
                        //      Log.d(TAG,"BT_RSSI:$rssi")
                    }else{
                        Toast.makeText(context,"何もスキャンされませんでした",Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}


