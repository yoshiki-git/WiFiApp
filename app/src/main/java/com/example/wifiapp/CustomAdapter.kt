package com.example.wifiapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val wifi_list :List<Wifi_Info>):RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    private val TAG = "CustomAdapter"

    // Viewの初期化
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView
        val ssid: TextView
        val bssid: TextView
        val level: TextView
        val freq :TextView

        init {
            image = view.findViewById(R.id.image)
            ssid = view.findViewById(R.id.ssid)
            bssid = view.findViewById(R.id.bssid)
            level = view.findViewById(R.id.level)
            freq = view.findViewById(R.id.freq)
        }
    }
    // レイアウトの設定
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item, viewGroup, false)
        return ViewHolder(view)
    }


    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val wifi = wifi_list[position]
        Log.d(TAG,wifi.level.toString())
        Log.d(TAG,wifi.freq.toString())

        viewHolder.image.setImageResource(wifi.ImageId)
        viewHolder.ssid.text = wifi.ssid
        viewHolder.bssid.text = wifi.bssid
        viewHolder.level.text ="${wifi.level}dBm"
        viewHolder.freq.text = "${wifi.freq}MHz"
    }

    // 表示数を返す
    override fun getItemCount() = wifi_list.size
}