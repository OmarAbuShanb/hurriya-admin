package dev.anonymous.hurriya.admin.presentation.screens.main.news

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.navArgs
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentAddNewsBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.UploadFileCallback
import dev.anonymous.hurriya.admin.domain.models.News
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral

class AddNewsFragment : BaseFragment<FragmentAddNewsBinding>(FragmentAddNewsBinding::inflate) {
    private lateinit var firebaseController: FirebaseController
    private val args: AddNewsFragmentArgs by navArgs()

    private var imageUri: Uri? = null
    private var imageName: String? = null

    private var id: String? = null
    private var oldImageUrl: String? = null
    private var title: String? = null
    private var details: String? = null
    private var newImageUrl: String? = null

    private var deleteOldImage = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setUpListener()
        putDate()
    }

    private fun setUpListener() {
        binding.btnAddNews.setOnClickListener {
            performSetNews()
        }

        binding.buChooseImageNews.setOnClickListener {
            requestPermissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun putDate() {
        args.model?.let {
            id = it.id
            oldImageUrl = it.imageUrl

            UtilsGeneral.instance!!
                .loadImage(requireContext(), it.imageUrl!!)
                .into(binding.ivImageNews)

            binding.etTitle.setText(it.title)
            binding.etDetails.setText(it.details)

            setAppBarTitle(R.string.update_the_news)
            binding.btnAddNews.setText(R.string.update)
        }
    }

    private fun checkData(title: String?, description: String?): Boolean {
        return !TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) // If the user comes to add data
                && (imageUri != null // Or comes to update data
                || oldImageUrl != null)
    }

    private fun performSetNews() {
        val title = binding.etTitle.getText().toString().trim()
        val details = binding.etDetails.getText().toString().trim()

        if (checkData(title, details)) {
            this.title = title
            this.details = details

            showLoadingDialog()
            checkFollowingProcess()
        }
    }

    private fun uploadImage() {
        firebaseController.uploadNewsImage(imageUri!!, imageName!!, object : UploadFileCallback {
            override fun onSuccess(fileUrl: String) {
                imageUri = null
                newImageUrl = fileUrl

                checkFollowingProcess()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun deleteNewsImage(imageUrl: String) {
        firebaseController.deleteFileUsingUrl(imageUrl, object : FirebaseCallback {
            override fun onSuccess() {
                dismissDialogAndFinishSuccessfully()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun dismissDialogAndFinishSuccessfully() {
        UtilsGeneral.instance!!
            .showToast(requireContext(), getString(R.string.task_completed_successfully))
        dismissLoadingDialog()
        closeCurrentFragment()
    }

    private val getNewsImage = registerForActivityResult(GetContent()) { result ->
        if (result != null) {
            imageUri = result
            imageName = UtilsGeneral.instance!!.getFileName(result, requireContext())
            binding.ivImageNews.setImageURI(result)

            if (oldImageUrl != null) {
                deleteOldImage = true
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getNewsImage.launch("image/*")
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Log.d(
                    TAG,
                    "requestPermissionLauncher: " + Manifest.permission.READ_EXTERNAL_STORAGE + "  DENIED"
                )
            } else {
                Log.d(
                    TAG,
                    "requestPermissionLauncher: " + Manifest.permission.READ_EXTERNAL_STORAGE + "  PERMANENTLY DENIED"
                )
            }
        }
    }

    private fun checkFollowingProcess() {
        if (imageUri != null) {
            uploadImage()
        } else {
            this.news = this.news
        }
    }

    private fun checkIfAdminChangeNewsImage() {
        if (deleteOldImage) {
            deleteNewsImage(oldImageUrl!!)
        } else {
            dismissDialogAndFinishSuccessfully()
        }
    }

    private var news: News
        get() {
            val imageUrl = if (newImageUrl != null) newImageUrl else oldImageUrl

            return News(imageUrl, title, details)
        }
        set(news) {
            firebaseController.setNews(news, object : FirebaseCallback {
                override fun onSuccess() {
                    checkIfAdminChangeNewsImage()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
        }

    companion object {
        private const val TAG = "AddNewsActivity"
    }
}