package com.example.wifiapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager

class RepeatMailService : Service() {

    private lateinit var context: Context
    val TAG = "RepeatMailService"
    private var interval:Int = 0
    private lateinit var motoadd:String
    private lateinit var motopass:String
    private lateinit var ateadd:String
    private lateinit var filePath:String
    private lateinit var fileName:String



    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"onCreate")
        //context取得
        context=applicationContext

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        //前回保存した値を読み出して表示する
        motoadd = pref.getString("MOTOADD","non").toString()
        motopass = pref.getString("PASS","non").toString()
        ateadd = pref.getString("ATEADD","non").toString()
        interval = pref.getInt("INTERVAL",15)
        filePath = pref.getString("FILEPATH","添付ファイルのパスがここに表示される").toString()
        fileName = pref.getString("FILENAME","ファイル名がここに表示される").toString()
        Log.d(TAG,motoadd)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val requestCode = intent!!.getIntExtra("REQUEST_CODE", 0)
        Log.d(TAG,"onStartCommand")

        Log.d(TAG,interval.toString())
        //    val context = applicationContext
        val channelId = "RepeatMail"
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
            .setContentText("Wifiログメール送信機能起動中")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .build()

        // startForeground 第一引数のidで通知を識別
        startForeground(9998, notification)

        //intervalが不正な値の場合はメール送信を行わない
        if (interval>1) {
            //毎回Alarmの設定 interval分毎に繰り返しを設定
            setNextAlarmService(context,interval)
            val sendMailService = SendMailService(motoadd,motopass,ateadd,filePath,fileName)
            sendMailService.sendOnce(context)
            Log.d(TAG,"メール送信")
        }else{
            Toast.makeText(context,"送信間隔の値が不正です",Toast.LENGTH_SHORT).show()
            stopSelf()
        }


        //return START_NOT_STICKY;
        //return START_STICKY;
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"onDestroy")
        //アラームの終了
        stopAlarmService()
        //サービス終了
        stopSelf()
    }

    // 次のアラームの設定
    private fun setNextAlarmService(context: Context,interval:Int) {

        // interval分毎のアラーム設定
        val repeatPeriod = (interval* 1000*60).toLong()
        val intent = Intent(context, RepeatMailService::class.java)
        val startMillis = System.currentTimeMillis() + repeatPeriod
        val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            startMillis, pendingIntent
        )
    }

    private fun stopAlarmService() {
        val indent = Intent(context, RepeatMailService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, indent, 0)

        // アラームを解除する
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}