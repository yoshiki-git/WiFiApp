package com.example.wifiapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wifiapp.databinding.FragmentSecondBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SecondFragment : Fragment() {

    private val TAG: String = "SecondFragment.kt"
    var mBTList = mutableListOf<BT_List>()

    var mAdapter = BT_CustomAdapter(mBTList)

    private lateinit var recyclerView:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

       recyclerView =binding.btRecyclerview
     //   mBTList = getBluetoothData()
        mBTList.add(BT_List("あ","い",-87,"う","え","お"))

        mAdapter = BT_CustomAdapter(mBTList)

        //境界線の設置
        val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager(context).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter

        val bt_scan:FloatingActionButton = binding.btnBtScan
        bt_scan.setOnClickListener {
            //ボタンの連打防止処理　2秒使えないようにする
            bt_scan.isEnabled = false
            Handler().postDelayed({ bt_scan.isEnabled = true }, 2000L)

            getBluetoothData()

      //      mAdapter = BT_CustomAdapter(mBTList)
      //      recyclerView.adapter = mAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        context?.unregisterReceiver(receiver)
        Log.d(TAG,"onDestroyView")
    }

    private fun getBluetoothData(){

        mBTList.clear()

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context?.registerReceiver(receiver, filter)

        bluetoothAdapter?.startDiscovery()
        Log.d(TAG,"register receiver")

     //   Toast.makeText(context,"Bluetoothスキャン中",Toast.LENGTH_SHORT).show()
     //   Thread.sleep(2000)
    //    bluetoothAdapter?.cancelDiscovery()

     //   Log.d(TAG,"BTList:${mBTList.toString()}")
     //   return mBTList
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

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
                        val deviceHardwareAddress = device.address // Bluetooth Hardware address
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

                        mBTList.add(BT_List(deviceName,deviceHardwareAddress,rssi,bondState,deviceType,deviceConType))



                        mAdapter = BT_CustomAdapter(mBTList)
                        recyclerView.adapter = mAdapter


                        Log.d(TAG,"Name:$deviceName")
                        Log.d(TAG,"hardwareAddress:$deviceHardwareAddress")
                  //      Log.d(TAG,"BT:bondState:${device?.bondState}")
                  //      Log.d(TAG,"BT:type:${device?.type}")
                        Log.d(TAG,"BT:deviceClass:${device?.bluetoothClass?.deviceClass}")
                  //      Log.d(TAG,"BT_RSSI:$rssi")

                        Log.d(TAG,"mBTList is ${mBTList.size.toString()}")
                    }else{
                        Toast.makeText(context,"何もスキャンされませんでした",Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }


}