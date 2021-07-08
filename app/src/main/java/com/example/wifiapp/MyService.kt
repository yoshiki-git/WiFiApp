package com.example.wifiapp

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import java.io.File
import java.lang.StringBuilder

class MyService : Service() {
    val TAG = "AppService"

    private lateinit var context: Context
    private lateinit var getLogData: GetLogData
    private lateinit var file :File
    private val getTimeData=GetTimeData()
    lateinit var wifiManager: WifiManager


    override fun onCreate() {
        super.onCreate()

        super.onCreate()
        //context取得
        context=applicationContext
        //getLogData生成
        getLogData= GetLogData(context)

        //ファイル名を現在時刻に設定する
        val start_time=getTimeData.getFileName()
        //拡張子をつける
        val fileName=start_time+"_Log"+".txt"
        file=getLogData.getFileStatus(fileName)

        wifiManager=context.getSystemService(Context.WIFI_SERVICE) as WifiManager
/*
        val wifiScanReceiver = object : BroadcastReceiver(){

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)
        */

    }



    override fun onDestroy() {
        super.onDestroy()
        //アラームの終了
        stopAlarmService()
        //サービス終了
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val requestCode = intent!!.getIntExtra("REQUEST_CODE", 0)
        //    val context = applicationContext
        val channelId = "default"
        val title = context.getString(R.string.app_name)

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        // Notification　Channel 設定
        val channel = NotificationChannel(
            channelId, title, NotificationManager.IMPORTANCE_DEFAULT
        )

        notificationManager.createNotificationChannel(channel)
        val notification = Notification.Builder(context, channelId)
            .setContentTitle(title) // android標準アイコンから
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentText("Monitoring Now")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .build()

        // startForeground 第一引数のidで通知を識別
        startForeground(9999, notification)

        val success = wifiManager.startScan()
        if (success) {
            // scan failure handling
            scanSuccess()
        }else{
            scanFailure()
        }

        //毎回Alarmの設定
        setNextAlarmService(context)

        //return START_NOT_STICKY;
        //return START_STICKY;
        return START_REDELIVER_INTENT
    }
    // 次のアラームの設定
    private fun setNextAlarmService(context: Context) {

        // 30s毎のアラーム設定
        val repeatPeriod = (30 * 1000).toLong()
        val intent = Intent(context, MyService::class.java)
        val startMillis = System.currentTimeMillis() + repeatPeriod
        val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            startMillis, pendingIntent
        )
    }

    private fun stopAlarmService() {
        val indent = Intent(context, MyService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, indent, 0)

        // アラームを解除する
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }


    fun scanSuccess() {
        val results = wifiManager.scanResults
        Toast.makeText(this,"Scanしたよ", Toast.LENGTH_SHORT).show()
        val stringBuilder = StringBuilder()
        stringBuilder.append(getTimeData.getNowTime())
            .append(",")
            .append("$results")
            .append("\n")
        getLogData.getLog(file,stringBuilder.toString())
        Log.d(TAG,"wi-fi scan succeeded")
    }

    fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        //Doze中、画面消灯時にはWifiスキャンは失敗する
        //位置情報オフでも失敗する
        Toast.makeText(this,"WifiScanが失敗しました", Toast.LENGTH_SHORT).show()
        val stringBuilder = StringBuilder()
        stringBuilder.append(getTimeData.getNowTime())
                     .append(",")
                     .append("wi-fi scan failed")
                     .append("\n")
        getLogData.getLog(file,stringBuilder.toString())
        Log.d(TAG,"wi-fi scan failed")

    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}