package com.example.wifiapp

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
import com.example.wifiapp.databinding.FragmentMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.security.AccessController.getContext


class MainFragment : Fragment() {

    private val TAG = "MainFragment.kt"

    var  mWifiList = mutableListOf<Wifi_Info>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val getDeviceData= context?.let { GetDeviceData(it) }

        if (getDeviceData != null) {
            mWifiList = getDeviceData.getWifiData()
        }

        val recyclerView=binding.recyclerView
        var mAdapter = CustomAdapter(mWifiList)

        //境界線の設置
        val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager(context).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter



        //Wifiビューの更新ボタン
        val wifi_scan: FloatingActionButton = binding.btnWifiScan
        wifi_scan.setOnClickListener {
            //ボタンの連打防止処理　2秒使えないようにする
            wifi_scan.isEnabled = false
            Handler().postDelayed({ wifi_scan.isEnabled = true }, 2000L)

            //ログ取得中ならスキャンは実行せずに直近のスキャン結果を返す
            if(context?.let { it1 -> getDeviceData?.mySerCheck(it1) } == true){
                Toast.makeText(context,"ログサービス起動中のため、直近のスキャン結果からビューを更新します",Toast.LENGTH_LONG).show()
                Log.d(TAG,"view create from serviceLog")
                mWifiList = getDeviceData?.scanSuccess()!!
                mAdapter = CustomAdapter(mWifiList)
                recyclerView.adapter = mAdapter
                return@setOnClickListener
            }

            //ログを取得していない場合はスキャンを実施し、最新のスキャン結果をビューに反映させる
            if (getDeviceData != null) {
                if(getDeviceData.scanJudge()){
                    Toast.makeText(context,"wifi scan 成功", Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"wifi scan succeeded")
                    mWifiList = getDeviceData?.scanSuccess()!!
                    mAdapter = CustomAdapter(mWifiList)
                    recyclerView.adapter = mAdapter
                }else{
                    Toast.makeText(context,"wifi scan 失敗", Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"wifi scan failed")
                }

            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}