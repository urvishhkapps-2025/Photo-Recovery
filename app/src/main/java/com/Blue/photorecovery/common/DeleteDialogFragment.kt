package com.Blue.photorecovery.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.Blue.photorecovery.R
import com.Blue.photorecovery.databinding.DeleteDialogBinding
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

class DeleteDialogFragment(count: Int, val decision: (decision: Boolean) -> Unit) :
    DialogFragment(),
    View.OnClickListener {
    private lateinit var display: DeleteDialogBinding

    var count = count

    @SuppressLint("MissingInflatedId")
    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        display = DeleteDialogBinding.inflate(layoutInflater)
        return display.root
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        display.apply {
            btnOk.setOnClickListener(this@DeleteDialogFragment)
            btnNo.setOnClickListener(this@DeleteDialogFragment)
            deleteCount.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
                text = "Delete ($count Photos)"
            }
            textContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            btnOk.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            btnNo.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            if (layContainer.viewTreeObserver != null) {
                layContainer.viewTreeObserver
                    .addOnGlobalLayoutListener {
                        YoYo.with(Techniques.ZoomIn).pivot(Float.MAX_VALUE, Float.MAX_VALUE)
                            .duration(500).delay(0).repeat(0).playOn(
                                layContainer
                            )
                    }
            }
        }
    }

    public override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft: FragmentTransaction = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (th: Exception) {
        }
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        try {
            if ((dialog != null) && (dialog!!.window != null)) {
                dialog!!.window!!.attributes.windowAnimations =
                    R.style.PurchaseDialogAnimation
                dialog!!.window!!
                    .setBackgroundDrawable(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.black_70_per
                        ).toDrawable()
                    )
                dialog!!.window!!.clearFlags(1024)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun onClick(v: View) {
        when (v.id) {
            R.id.btnOk -> try {
                if (dialog != null && dialog!!.isShowing) {
                    dialog!!.dismiss()
                    decision.invoke(true)
                    return
                }
                return
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }

            R.id.btnNo -> try {
                if (dialog != null && dialog!!.isShowing) {
                    dialog!!.dismiss()
                    return
                }
                return
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        }
    }

}