package com.Blue.photorecovery.announcement

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.Blue.photorecovery.R
import com.Blue.photorecovery.common.LinearGradientSpan
import com.Blue.photorecovery.databinding.CannyBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class Canny(context: Context, val decision: (decision: Boolean) -> Unit) :
    BottomSheetDialogFragment() {

    private lateinit var display: CannyBinding
    private var context = context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        display = CannyBinding.inflate(layoutInflater)
        return display.root
    }

    override fun getTheme(): Int {
        return R.style.TransparentBottomSheetDialog
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        display.apply {

            textPermission.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            textTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
            textDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            btnAgree.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)

            setGradientOnlyOnEasyRecovery(textDescription)

            btnAgree.setOnClickListener {
                try {
                    if (dialog != null && dialog!!.isShowing) {
                        dialog!!.dismiss()
                        decision.invoke(true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            btnClose.setOnClickListener {
                try {
                    if (dialog != null && dialog!!.isShowing) {
                        dialog!!.dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

    }

    fun setGradientOnlyOnEasyRecovery(tv: TextView) {
        val fullText =
            "Easy Recovery Requires Full Access To Your Device Storage To Locate And Recover Deleted Or Lost Files."
        val target = "Easy Recovery"
        val start = fullText.indexOf(target)
        val end = start + target.length

        if (start >= 0) {
            val ss = SpannableString(fullText)
            ss.setSpan(
                LinearGradientSpan(
                    Color.parseColor("#005EEC"), // orange
                    Color.parseColor("#67DCFC")  // pink
                ),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = ss
        } else {
            tv.text = fullText // fallback
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val contentView = View.inflate(context, R.layout.canny, null)
        dialog.setContentView(contentView)

        val parent = contentView.parent as View
        parent.setBackgroundColor(resources.getColor(R.color.transparent_color))

        // Set the dialog to be full-screen
        val layoutParams = parent.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
        val behavior = layoutParams.behavior
        if (behavior is BottomSheetBehavior<*>) {
            val bottomSheetBehavior = behavior as BottomSheetBehavior<*>
            (parent as View).setBackgroundColor(resources.getColor(R.color.transparent_color))
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.skipCollapsed = true
            bottomSheetBehavior.isFitToContents = false
            bottomSheetBehavior.isDraggable = false
            bottomSheetBehavior.isFitToContents = true
        }
        parent.layoutParams = layoutParams

        return dialog
    }

}