package dev.anonymous.hurriya.admin.presentation.dialogs

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.DialogGetTextBinding

class GetTextDialog : DialogFragment() {
    private lateinit var binding: DialogGetTextBinding
    private val args: GetTextDialogArgs by navArgs()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setWindowAnimations(R.style.DialogTranslateScaleAnimation)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogGetTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        with(binding) {
            tvDialogTitle.setText(args.titleRes)
            edDialog.setHint(args.hintRes)
            btnDialog.setText(args.buttonTextRes)

            btnDialog.setOnClickListener {
                val text = edDialog.text.toString().trim()
                if (!TextUtils.isEmpty(text)) {
                    sendResultBack(text)
                    dismiss()
                }
            }
        }
    }

    private fun sendResultBack(text: String) {
        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.set(args.resultKey, text)
    }
}
