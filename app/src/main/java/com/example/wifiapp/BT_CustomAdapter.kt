package com.example.wifiapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BT_CustomAdapter(private val bt_list :List<BT_List>):RecyclerView.Adapter<BT_CustomAdapter.ViewHolder>() {
    private val TAG = "BT_CustomAdapter.kt"

    // Viewの初期化
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bt_image: ImageView
        val bt_textview1: TextView  //デバイス名　rssi txpower
        val bt_textview2: TextView  //デバイスタイプ　ベンダー名　通信方式
        val bt_textview3: TextView  //Hardwareアドレス
        init {
            bt_image = view.findViewById(R.id.bt_imageView)
            bt_textview1 = view.findViewById(R.id.bt_textview1)
            bt_textview2 = view.findViewById(R.id.bt_textview2)
            bt_textview3 = view.findViewById(R.id.bt_textview3)
        }
    }
    // レイアウトの設定
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.bt_list_item, viewGroup, false)
        return ViewHolder(view)
    }


    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val bt = bt_list[position]

        val name =if (bt.name != null){
            bt.name
        }else{
            "null"
        }
        val macAddress = bt.Hardware_Address
        val rssi = bt.rssi
        val bondState = bt.bondState
        val deviceType = bt.deviceType
        val deviceConType = bt.deviceConType

        viewHolder.bt_image.setImageResource(R.drawable.bluetooth)
        viewHolder.bt_textview1.text = "Name:$name $bondState RSSI:$rssi"
        viewHolder.bt_textview2.text = "Type:$deviceType Tech:$deviceConType"
        viewHolder.bt_textview3.text = "Bluetooth Hardware Address:$macAddress"
    }

    // 表示数を返す
    override fun getItemCount() = bt_list.size
}