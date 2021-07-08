package com.example.wifiapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder

class MyService : Service() {
    val TAG = "TestApp"

    private lateinit var context: Context
    private lateinit var getLogData: GetLogData
    private val getTimeData=GetTimeData()

    override fun onCreate() {
        super.onCreate()

        super.onCreate()
        //context取得
        context=applicationContext
        //getLogData生成
        getLogData= GetLogData(context)
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

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}