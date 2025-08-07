package dev.anonymous.hurriya.admin.presentation.screens.main.videos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentAddVideoBinding
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.FirebaseCallback
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController.UploadFileCallback
import dev.anonymous.hurriya.admin.domain.models.Video
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.services.UploadVideosService
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class AddVideoFragment : BaseFragment<FragmentAddVideoBinding>(FragmentAddVideoBinding::inflate) {
    private lateinit var firebaseController: FirebaseController
    private val args: AddVideoFragmentArgs by navArgs()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firebaseStorage: FirebaseStorage

    private var mBroadcastReceiver: BroadcastReceiver? = null

    private var videoImageBitmap: Bitmap? = null
    private var videoUri: Uri? = null
    private var videoImageUri: Uri? = null
    private var videoName: String? = null
    private var videoImageName: String? = null

    private var newVideoImageUrl: String? = null
    private var id: String? = null
    private var oldVideoUrl: String? = null
    private var oldVideoImageUrl: String? = null
    private var title: String? = null

    private var deleteOldVideoImage = false
    private var deleteOldVideo = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun uploadVideoToFirebase(videoUri: Uri, videoTitle: String?) {
        val videoRef = firebaseStorage.getReference()
            .child("videos")
            .child(firebaseAuth.uid!!)
            .child(UUID.randomUUID().toString())
            .child("video.mp4")

        val metadata = StorageMetadata.Builder()
            .setCustomMetadata("title", videoTitle)
            .build()

        videoRef.putFile(videoUri, metadata)
            .addOnSuccessListener { taskSnapshot ->
                Log.d("Upload", "Video uploaded successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Upload", "Failed to upload video", e)
            }
    }

    private fun init() {
        firebaseController = FirebaseController.instance!!

        putDataIfExist()
        setUpListener()
        setUpBroadcastReceiver()
    }

    private fun setUpBroadcastReceiver() {
        // Local broadcast receiver
        mBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "onReceive:$intent")

                when (intent.action) {
                    UploadVideosService.UPLOAD_COMPLETED, UploadVideosService.UPLOAD_ERROR -> {
                        //        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);
                    }
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()

        // Register receiver for uploads
        val manager = LocalBroadcastManager.getInstance(requireContext())
        manager.registerReceiver(mBroadcastReceiver!!, UploadVideosService.intentFilter)
    }

    public override fun onStop() {
        super.onStop()

        // Unregister uploads receiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mBroadcastReceiver!!)
    }

    private fun uploadVideoInForeground() {
        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        val intentService: Intent = Intent(requireContext(), UploadVideosService::class.java)
            .putExtra(UploadVideosService.EXTRA_VIDEO_URI, videoUri)
            .putExtra(
                UploadVideosService.EXTRA_VIDEO_NAME,
                videoName
            ) // getVideo() => videoUrl is null
            .putExtra(UploadVideosService.EXTRA_VIDEO_MODEL, this.video)
            .putExtra(UploadVideosService.EXTRA_IS_OLD_VIDEO_IMAGE_DELETED, deleteOldVideoImage)
            .putExtra(UploadVideosService.EXTRA_IS_OLD_VIDEO_DELETED, deleteOldVideo)
            .putExtra(UploadVideosService.EXTRA_OLD_VIDEO_IMAGE_URL, oldVideoImageUrl)
            .setAction(UploadVideosService.ACTION_UPLOAD)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intentService)
        } else {
            ContextCompat.startForegroundService(requireContext(), intentService)
        }
        dismissDialogAndFinishSuccessfully(getString(R.string.video_uploading_in_background))
    }

    private fun putDataIfExist() {
        args.model?.let {
            id = it.id
            oldVideoUrl = it.videoUrl
            oldVideoImageUrl = it.videoUrl

            UtilsGeneral.instance!!
                .loadImage(requireContext(), it.videoImageUrl!!)
                .into(binding.ivVideoImage)

            binding.tvChooseVideo.setText(R.string._1_file_chooses)
            binding.etTitle.setText(it.title)

            setAppBarTitle(R.string.update_the_video_data)
            binding.btnAddVideo.setText(R.string.update)
        }
    }

    private fun setUpListener() {
        binding.buChooseVideo.setOnClickListener {
            getVideoLauncher.launch("video/*")
        }

        binding.buChooseVideoImage.setOnClickListener {
            getVideoImageLauncher.launch("image/*")
        }

//        binding.btnAddVideo.setOnClickListener{
//            performSetVideo()
//        }

        binding.btnAddVideo.setOnClickListener {
            uploadVideoToFirebase(
                videoUri!!,
                binding.etTitle.getText().toString()
            )
        }
    }

    private fun checkData(title: String?): Boolean {
        return !TextUtils.isEmpty(title) &&  // If the user comes to add data
                // The user must have chosen either videoImageUri or videoImageBitmap
                (((videoImageUri != null || videoImageBitmap != null) && videoUri != null) // Or comes to update data
                        || (oldVideoImageUrl != null && oldVideoUrl != null))
    }

    private fun performSetVideo() {
        val title = binding.etTitle.getText().toString().trim()

        if (checkData(title)) {
            this.title = title

            // If the user comes to add data
            if (id == null) {
                id = UUID.randomUUID().toString()
            }

            showLoadingDialog()
            checkFollowingProcess()
        }
    }

    private fun uploadVideoImage() {
        firebaseController.uploadVideoFile(
            id!!,
            videoImageUri!!,
            videoImageName!!,
            object : UploadFileCallback {
                override fun onSuccess(fileUrl: String) {
                    videoImageUri = null
                    newVideoImageUrl = fileUrl

                    checkFollowingProcess()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
    }

    private fun uploadVideoImageBitmap() {
        firebaseController.uploadVideoImageBitmap(
            id!!,
            videoImageBitmap!!,
            object : UploadFileCallback {
                override fun onSuccess(fileUrl: String) {
                    videoImageBitmap = null
                    newVideoImageUrl = fileUrl

                    checkFollowingProcess()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
    }

    private fun deleteFileUsingUrl(fileUrl: String) {
        firebaseController.deleteFileUsingUrl(fileUrl, object : FirebaseCallback {
            override fun onSuccess() {
                dismissDialogAndFinishSuccessfully(getString(R.string.task_completed_successfully))
            }

            override fun onFailure(errorMessage: String) {
                dismissLoadingDialog()
            }
        })
    }

    private fun dismissDialogAndFinishSuccessfully(toastMessage: String) {
        UtilsGeneral.instance!!.showToast(requireContext(), toastMessage)
        dismissLoadingDialog()
        closeCurrentFragment()
    }

    private val getVideoLauncher = registerForActivityResult(GetContent()) { result ->
        if (result != null) {
            videoUri = result

            videoName = UtilsGeneral.instance!!.getFileName(result, requireContext())
            setThumbnailVideoIfAllow(result)

            // if user come to edit and changes the pdf
            if (oldVideoUrl != null) {
                deleteOldVideo = true
            } else {
                binding.tvChooseVideo.setText(R.string._1_file_chooses)
            }
        }
    }

    private fun setThumbnailVideoIfAllow(contentUri: Uri) {
        val thumbnail = UtilsGeneral.getThumbnailVideo(requireContext(), contentUri)
        if (thumbnail != null) {
            videoImageBitmap = thumbnail
            videoImageUri = null

            binding.ivVideoImage.setImageBitmap(thumbnail)

            if (oldVideoImageUrl != null) {
                deleteOldVideoImage = true
            }
        }
    }

    private val getVideoImageLauncher = registerForActivityResult(GetContent()) { result ->
        if (result != null) {
            videoImageUri = result
            videoImageBitmap = null

            videoImageName = UtilsGeneral.instance!!.getFileName(result, requireContext())
            binding.ivVideoImage.setImageURI(result)

            if (oldVideoImageUrl != null) {
                deleteOldVideoImage = true
            }
        }
    }

    private fun checkFollowingProcess() {
        if (videoImageUri != null) {
            uploadVideoImage()
        } else if (videoImageBitmap != null) {
            uploadVideoImageBitmap()
        } else if (videoUri != null) {
            uploadVideoInForeground()
        } else {
            this.video = this.video
        }
    }

    private fun checkIfAdminChangeVideoImage() {
        if (deleteOldVideoImage) {
            deleteFileUsingUrl(oldVideoImageUrl!!)
        } else {
            dismissDialogAndFinishSuccessfully(getString(R.string.task_completed_successfully))
        }
    }

    private var video: Video
        get() {
            val videoImageUrl = if (newVideoImageUrl != null) newVideoImageUrl else oldVideoImageUrl

            return Video(
                id!!, oldVideoUrl, videoImageUrl, title, Timestamp.now()
            )
        }
        set(video) {
            firebaseController.setVideo(video, object : FirebaseCallback {
                override fun onSuccess() {
                    checkIfAdminChangeVideoImage()
                }

                override fun onFailure(errorMessage: String) {
                    dismissLoadingDialog()
                }
            })
        }

    companion object {
        private const val TAG = "AddVideoActivity"
    }
} // User does not have permission to access this object.