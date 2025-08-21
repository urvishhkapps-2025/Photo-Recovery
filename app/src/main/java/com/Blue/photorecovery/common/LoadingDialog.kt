package com.Blue.photorecovery.common

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.Blue.photorecovery.R
import com.Blue.photorecovery.databinding.HoldScreenBinding
import java.util.Objects

class LoadingDialog : DialogFragment() {

    lateinit var viewBind: HoldScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBind = HoldScreenBinding.inflate(layoutInflater)
        return viewBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Objects.requireNonNull(
                Objects.requireNonNull(
                    dialog
                )?.window
            )?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            Objects.requireNonNull(
                Objects.requireNonNull(
                    dialog
                )?.window
            )?.statusBarColor = resources.getColor(R.color.transparent_color)
            Objects.requireNonNull(
                Objects.requireNonNull(
                    dialog
                )?.window
            )?.navigationBarColor = resources.getColor(R.color.transparent_color)
        }

        viewBind.apply {
            loading.loop(true)
            loading.setAnimation(R.raw.loading)
            loading.playAnimation()
        }

    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (th: Exception) {
            th.printStackTrace()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        try {
            if ((dialog != null) && (dialog!!.window != null)
            ) {
                dialog!!.window!!.attributes.windowAnimations = R.style.PurchaseDialogAnimation
                dialog!!.window!!
                    .setBackgroundDrawable(
                        ContextCompat.getColor(
                            (activity)!!,
                            R.color.transparent_color
                        ).toDrawable()
                    )
                dialog!!.window!!.clearFlags(1024)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}