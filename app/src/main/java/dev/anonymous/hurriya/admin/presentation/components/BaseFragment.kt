package dev.anonymous.hurriya.admin.presentation.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.presentation.navigation.navigateSafe

abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {
    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundResource(R.color.white)
    }

    fun setAppBarTitle(@StringRes title: Int) {
        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(title)
    }

    fun navigateTo(directions: NavDirections) {
        findNavController().navigateSafe(directions)
    }

    fun closeCurrentFragment() {
        findNavController().popBackStack()
    }

    fun showLoadingDialog() {
        with(findNavController()) {
            if (currentDestination?.id != R.id.loadingDialog) {
                navigate(R.id.loadingDialog)
            }
        }
    }

    fun dismissLoadingDialog() {
        with(findNavController()) {
            if (currentDestination?.id == R.id.loadingDialog) {
                popBackStack()
            }
        }
    }
}
