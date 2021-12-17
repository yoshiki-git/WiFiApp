package com.example.wifiapp

import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class GetTimeData {
    val TAG="GetTimeData"

    //時刻取得　表示用日付有り
    fun getNowDate():String{
        val df: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date= Date(System.currentTimeMillis())
        return df.format(date)
    }

    //時刻を取得する関数　ログデータ用日付無し
    fun getNowTime(): String {
        val df: DateFormat = SimpleDateFormat("HH:mm:ss")
        val date = Date(System.currentTimeMillis())
        return df.format(date)
    }

    //時刻を取得する関数　ファイル名用日付と/と:が無い
    fun getFileName(): String {
        val df: DateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val date = Date(System.currentTimeMillis())
        return df.format(date)
    }

    fun compareTime(hour:Int,minute:Int) : Boolean{
        val nowTime : LocalTime = LocalTime.now()
        val date1 : LocalTime = LocalTime.of(hour,minute)

        return nowTime.isBefore(date1)
    }

    //経過時間を取得する関数
    fun getElapsedTime(startTime: Long): Double {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        Log.d(TAG, "経過時間:$elapsedTime")
        return elapsedTime / 1000.000
    }

    //経過時間を取得する関数（View用)
    fun getElapsedViewTime(startTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        val elapsedTimeSecond = elapsedTime / 1000
        val hour = elapsedTimeSecond / 3600
        val minite = elapsedTimeSecond % 3600 / 60
        val second = elapsedTimeSecond % 60
        return hour.toString() + "時間" + minite + "分" + second + "秒"
    }

}