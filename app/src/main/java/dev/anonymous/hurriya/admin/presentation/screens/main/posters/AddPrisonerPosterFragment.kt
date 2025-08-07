package dev.anonymous.hurriya.admin.presentation.screens.main.posters

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentAddPrisonerPosterBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.UploadFileCallback
import dev.anonymous.hurriya.admin.domain.models.Poster
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import java.util.UUID

class AddPrisonerPosterFragment :
    BaseFragment<FragmentAddPrisonerPosterBinding>(FragmentAddPrisonerPosterBinding::inflate) {
    private lateinit var firebaseController: FirebaseController

    private var imageUri: Uri? = null
    private var imageName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setUpListener()
    }

    private fun setUpListener() {
        binding.buChoosePosterImage.setOnClickListener {
            getPosterImageLauncher.launch("image/*")
        }

        binding.btnAddPoster.setOnClickListener {
            performSetPoster()
        }
    }

    private fun performSetPoster() {
        if (imageUri != null) {
            showLoadingDialog()
            uploadImage()
        }
    }

    private fun uploadImage() {
        firebaseController.uploadPosterImage(
            imageUri!!,
            imageName!!,
            object : UploadFileCallback {
                override fun onSuccess(fileUrl: String) {
                    setPoster(getPoster(fileUrl))
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
    }

    private fun setPoster(poster: Poster) {
        firebaseController.setPoster(poster, object : FirebaseCallback {
            override fun onSuccess() {
                dismissDialogAndFinishSuccessfully()
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun getPoster(imageUrl: String?): Poster {
        val id = UUID.randomUUID().toString()
        return Poster(id, imageUrl, Timestamp.now())
    }

    private fun dismissDialogAndFinishSuccessfully() {
        UtilsGeneral.instance!!.showToast(
            requireContext(),
            getString(R.string.task_completed_successfully)
        )
        dismissLoadingDialog()
        closeCurrentFragment()
    }

    private val getPosterImageLauncher = registerForActivityResult(GetContent()) { result ->
        if (result != null) {
            imageUri = result
            imageName = UtilsGeneral.instance!!.getFileName(result, requireContext())
            binding.ivPoster.setImageURI(result)
        }
    }
}