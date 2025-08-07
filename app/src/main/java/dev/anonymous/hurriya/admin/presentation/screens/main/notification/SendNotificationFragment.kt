package dev.anonymous.hurriya.admin.presentation.screens.main.notification

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.databinding.FragmentSendNotificationBinding
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class SendNotificationFragment :
    BaseFragment<FragmentSendNotificationBinding>(FragmentSendNotificationBinding::inflate) {

    @Inject
    lateinit var firebaseFunctions: FirebaseFunctions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        setUpListener()
    }

    private fun setUpListener() {
        binding.buPushNotification.setOnClickListener {
            val title = binding.etTitle.getText().toString().trim { it <= ' ' }
            val details = binding.etDetails.getText().toString().trim { it <= ' ' }

            if (checkData(title, details)) {
                sendNotification(title, details)
            }
        }
    }

    private fun checkData(title: String, details: String): Boolean {
        return !TextUtils.isEmpty(title) && !TextUtils.isEmpty(details)
    }

    private fun sendNotification(title: String, details: String) {
        val data: MutableMap<String, String> = HashMap()
        data.put("title", title)
        data.put("body", details)
        sendNewsNotification(data)
    }

    private fun sendNewsNotification(data: MutableMap<String, String>) {
        showLoadingDialog()
        firebaseFunctions
            .getHttpsCallable("sendLatestNewsNotification")
            .call(data)
            .addOnSuccessListener { result ->
                Toast.makeText(requireContext(), "تم ارسال الاشعار بنجاح", Toast.LENGTH_SHORT)
                    .show()
                dismissLoadingDialog()
                closeCurrentFragment()
            }.addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "حدث خطأ ما :(\n" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
                dismissLoadingDialog()
            }
    }
}