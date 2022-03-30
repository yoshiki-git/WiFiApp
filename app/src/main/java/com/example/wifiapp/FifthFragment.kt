package com.example.wifiapp

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wifiapp.databinding.FragmentFifthBinding
import java.lang.StringBuilder


//Battery情報取得用フラグメント
//
//Battery情報取得用フラグメント
//
class FifthFragment : Fragment() {

    private val TAG : String = "FifthFragment.kt"

    private var _binding: FragmentFifthBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFifthBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //バッテリー情報を取得するフィルターを定義
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context?.registerReceiver(null, ifilter)
        }

        //バッテリーステータスの定義
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        //電池残量
        val batteryPct: Float? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }

        //バッテリー温度
        val temp :Float? = batteryStatus?.let { intent ->
            val tempInt : Int = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1)
            tempInt/10.0f
        }

        val charge_text = if (isCharging == true){
            "状態：充電中"
        }else{
            "状態：電源接続なし"
        }

        val sb = StringBuilder()
        sb.append("電池残量:${batteryPct}%")
            .append("\n")
            .append("")
            .append(charge_text)
            .append("\n")
            .append("バッテリー温度：${temp}℃")

        binding.textViewButtery.text = sb.toString()

        /*
        binding.textViewButtery.text = "電池残量：${batteryPct}%"
        if (isCharging == true){
            binding.textViewButtery2.text = "状態：充電中"
        }else{
            binding.textViewButtery2.text = "状態：電源接続なし"
        }

         */



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        Log.d(TAG,"onDestroyView")
    }
}