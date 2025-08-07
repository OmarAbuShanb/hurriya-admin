package dev.anonymous.hurriya.admin.presentation.screens.main.album

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentAddAlbumBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.UploadFileCallback
import dev.anonymous.hurriya.admin.domain.models.Album
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import java.util.UUID

class AddAlbumFragment : BaseFragment<FragmentAddAlbumBinding>(FragmentAddAlbumBinding::inflate) {
    private lateinit var firebaseController: FirebaseController

    private val args: AddAlbumFragmentArgs by navArgs()

    private var imageUri: Uri? = null
    private var imageName: String? = null

    private var uuid: String? = null
    private var oldImageUrl: String? = null
    private var title: String? = null
    private var newImageUrl: String? = null

    private var deleteOldImage = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        setUpListener()
        putDataIfExist()
    }

    private fun setUpListener() {
        binding.buChooseImageAlbum.setOnClickListener {
            getAlbumImageLauncher.launch("image/*")
        }

        binding.btnAddAlbum.setOnClickListener {
            performSetAlbum()
        }
    }

    private fun putDataIfExist() {
        args.model?.let {
            uuid = it.id
            oldImageUrl = it.imageUrl

            UtilsGeneral.instance!!
                .loadImage(requireContext(), it.imageUrl!!)
                .into(binding.ivAlbumImage)

            binding.etAlbumTitle.setText(it.title)

            setAppBarTitle(R.string.update_album)
            binding.btnAddAlbum.setText(R.string.update)
        }
    }

    private fun checkData(title: String?): Boolean {
        return !TextUtils.isEmpty(title) &&  // If the user comes to add data or comes to update data
                (imageUri != null || oldImageUrl != null)
    }

    private fun performSetAlbum() {
        val title = binding.etAlbumTitle.getText().toString()
            .trim { it <= ' ' }

        if (checkData(title)) {
            showLoadingDialog()

            if (uuid == null) {
                uuid = UUID.randomUUID().toString()
            }

            this.title = title
            checkFollowingProcess()
        }
    }

    private fun uploadImage() {
        firebaseController.uploadAlbumImage(
            uuid!!,
            imageUri!!,
            imageName!!,
            object : UploadFileCallback {
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

    private fun deleteAlbumImage(imageUrl: String) {
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

    private val getAlbumImageLauncher = registerForActivityResult(GetContent()) { result: Uri? ->
        if (result != null) {
            imageUri = result
            imageName = UtilsGeneral.instance!!.getFileName(result, requireContext())
            binding.ivAlbumImage.setImageURI(result)

            if (oldImageUrl != null) {
                deleteOldImage = true
            }
        }
    }

    private fun checkFollowingProcess() {
        if (imageUri != null) {
            uploadImage()
        } else {
            this.album = this.album
        }
    }

    private fun checkIfAdminChangeAlbumImage() {
        if (deleteOldImage) {
            deleteAlbumImage(oldImageUrl!!)
        } else {
            dismissDialogAndFinishSuccessfully()
        }
    }

    private var album: Album
        get() {
            val imageUrl: String = (if (newImageUrl != null) newImageUrl else oldImageUrl)!!

            return Album(
                uuid!!, imageUrl, title, Timestamp.now()
            )
        }
        set(album) {
            firebaseController.setAlbum(album, object : FirebaseCallback {
                override fun onSuccess() {
                    checkIfAdminChangeAlbumImage()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
        }
}