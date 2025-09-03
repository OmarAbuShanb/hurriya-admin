package dev.anonymous.hurriya.admin.presentation.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.DialogLoadingBinding

class LoadingDialog : DialogFragment() {
    override fun onStart() {
        super.onStart()

        val dialog = getDialog()
        if (dialog != null) {
            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                window.setWindowAnimations(R.style.DialogAlphaScaleAnimation)
            }
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogLoadingBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }
}