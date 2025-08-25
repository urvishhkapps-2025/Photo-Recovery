package com.Blue.photorecovery.activity.storage

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.Blue.photorecovery.R
import com.Blue.photorecovery.adapter.common.RecoverPagerAdapter
import com.Blue.photorecovery.databinding.ActivityRecoveredHistoryBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RecoveredHistory : AppCompatActivity() {
    private lateinit var binding: ActivityRecoveredHistoryBinding

    private val TAB_TITLES = listOf("Photos", "Videos", "Audio")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoveredHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {


            viewPager.adapter = RecoverPagerAdapter(this@RecoveredHistory)

            // Attach with custom tab views
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                val v = layoutInflater.inflate(R.layout.view_tab_chip, null)
                v.findViewById<TextView>(R.id.tvTab).text = TAB_TITLES[position]
                tab.customView = v
            }.attach()

            for (i in 0 until tabLayout.tabCount) {
                val tabView = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                val lp = tabView.layoutParams as ViewGroup.MarginLayoutParams
                val spacing = (10 * resources.displayMetrics.density).toInt()
                lp.marginEnd = 2   // <-- space in dp, convert if needed
                lp.marginStart = spacing
                tabView.layoutParams = lp
            }

            // Make selection state drive background & text color
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tab.customView?.isSelected = true
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.customView?.isSelected = false
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            // Ensure first tab starts selected
            tabLayout.getTabAt(0)?.customView?.isSelected = true

            // Optional: sync ViewPager page change -> tab selected state (when swiping)
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    // TabLayoutMediator already selects tab; we just update customView state:
                    for (i in 0 until tabLayout.tabCount) {
                        tabLayout.getTabAt(i)?.customView?.isSelected = (i == position)
                    }
                }
            })
        }

    }

}