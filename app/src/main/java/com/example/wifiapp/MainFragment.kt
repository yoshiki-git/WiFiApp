package com.example.wifiapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wifiapp.databinding.FragmentMainBinding


class MainFragment : Fragment() {

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

        val getDeviceData = context?.let { GetDeviceData(it) }

        if (getDeviceData != null) {
            mWifiList = getDeviceData.getWifiData()
        }

        val recyclerView=binding.recyclerView
        val mAdapter = CustomAdapter(mWifiList)

        //境界線の設置
        val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager(context).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}