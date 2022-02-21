package com.example.wifiapp

import android.os.ParcelUuid

data class BT_List(
    var name: String,
    var MAC_Address: String,
    var rssi: Short,
    var bondState: String,
    var deviceType: String,
    var deviceConType: String,
)
