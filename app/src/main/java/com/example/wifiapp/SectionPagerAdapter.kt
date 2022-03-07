package com.example.wifiapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class SectionPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    //スワイプビューのページ数
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment =
        when(position) {
            0 -> {
                //Wifi情報
                MainFragment()
            }
            1 -> {
                //Bluetooth情報
                SecondFragment()
            }
            2 -> {
                //BLE情報
                ThirdFragment()
            }
            3 -> {
                //Cell情報
                FourthFragment()
            }
            4 -> {
                //Battery情報
                FifthFragment()
            }
            else -> {
                MainFragment()
            }
        }


}