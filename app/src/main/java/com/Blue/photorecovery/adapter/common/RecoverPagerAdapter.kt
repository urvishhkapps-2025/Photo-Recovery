package com.Blue.photorecovery.adapter.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.Blue.photorecovery.activity.fragment.AudioFragment
import com.Blue.photorecovery.activity.fragment.PhotoFragment
import com.Blue.photorecovery.activity.fragment.VideoFragment

private val TAB_TITLES = listOf("Photos", "Videos", "Audio")


    class RecoverPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = TAB_TITLES.size
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> PhotoFragment()
            1 -> VideoFragment()
            2 -> AudioFragment()
            else -> PhotoFragment()
        }

    }