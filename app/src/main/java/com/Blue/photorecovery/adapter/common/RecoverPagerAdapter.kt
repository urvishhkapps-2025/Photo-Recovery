package com.Blue.photorecovery.adapter.common

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.Blue.photorecovery.activity.fragment.PhotoFragment
import com.Blue.photorecovery.common.PageVisibility
import com.Blue.photorecovery.common.SelectionHost

class RecoverPagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    private val fragments = SparseArray<Fragment>()

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        val f: Fragment = when (position) {
            0 -> PhotoFragment.newInstance(1) // 1 = photos (your existing arg)
            1 -> PhotoFragment.newInstance(2) // 2 = videos
            else -> PhotoFragment.newInstance(3) // 3 = audio
        }
        fragments.put(position, f)
        return f
    }

    fun pageVisibilityAt(position: Int): PageVisibility? =
        fragments.get(position) as? PageVisibility


}
