package dev.anonymous.hurriya.admin.services

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import dev.anonymous.hurriya.admin.MainActivity
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.firebase.controller.FirebaseController
import dev.anonymous.hurriya.admin.firebase.enums.FirebaseCollection
import dev.anonymous.hurriya.admin.domain.models.Video

/**
 * Service to handle uploading files to Firebase Storage.
 */
class UploadVideosService : BaseTaskService() {
    private lateinit var firebaseController: FirebaseController

    override fun onCreate() {
        super.onCreate()

        firebaseController = FirebaseController.instance!!

        startTaskForeground(
            PROGRESS_NOTIFICATION_ID,
            progressNotification(getString(R.string.app_name), 1)
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent:$startId")

        if (ACTION_UPLOAD == intent.action) {
            val videoUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_VIDEO_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_VIDEO_URI)
            }

            val videoName = intent.getStringExtra(EXTRA_VIDEO_NAME)
            val videoModel: Video? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(EXTRA_VIDEO_MODEL, Video::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(EXTRA_VIDEO_MODEL) as? Video
            }

            val deleteOldVideoImage =
                intent.getBooleanExtra(EXTRA_IS_OLD_VIDEO_IMAGE_DELETED, false)
            val deleteOldVideo = intent.getBooleanExtra(EXTRA_IS_OLD_VIDEO_DELETED, false)
            val oldVideoImageUrl: String = intent.getStringExtra(EXTRA_OLD_VIDEO_IMAGE_URL)!!

            // Make sure we have permission to read the data
            /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContentResolver().takePersistableUriPermission(
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } */
            uploadVideoFromUri(
                videoUri!!,
                videoModel!!,
                videoName!!,
                oldVideoImageUrl,
                deleteOldVideoImage,
                deleteOldVideo
            )
        }

        return START_REDELIVER_INTENT
    }

    private fun uploadVideoFromUri(
        videoUri: Uri,
        video: Video,
        videoName: String,
        oldVideoImageUrl: String?,
        deleteOldVideoImage: Boolean,
        deleteOldVideo: Boolean
    ) {
        taskStarted()

        val ref = FirebaseStorage.getInstance()
            .getReference(FirebaseCollection.Videos.name)
            .child(video.id!!)
            .child(videoName)

        ref.putFile(videoUri)
            .addOnProgressListener { taskSnapshot ->
                // 100 * 5 / 5
                val progress = (100.0
                        * taskSnapshot.bytesTransferred
                        / taskSnapshot.totalByteCount).toInt()
                startTaskForeground(
                    PROGRESS_NOTIFICATION_ID,
                    progressNotification(video.title, progress)
                )
            }
            .continueWithTask { task ->
                if (!task.isSuccessful && task.exception != null) {
                    taskFailed(task.exception!!.message, video.title, videoUri)
                }
                ref.getDownloadUrl()
            }
            .addOnSuccessListener(OnSuccessListener { uri ->
                val oldVideoUrlIfExists = video.videoUrl
                video.videoUrl = uri.toString()
                setVideo(
                    video,
                    videoUri,
                    oldVideoImageUrl!!,
                    oldVideoUrlIfExists!!,
                    deleteOldVideoImage,
                    deleteOldVideo
                )
            })
            .addOnFailureListener { e ->
                taskFailed(
                    e.message,
                    video.title,
                    videoUri
                )
            }
    }

    private fun setVideo(
        video: Video,
        videoUri: Uri,
        oldVideoImageUrl: String,
        oldVideoUrl: String,
        deleteOldVideoImage: Boolean,
        deleteOldVideo: Boolean
    ) {
        firebaseController.setVideo(video, object : FirebaseController.FirebaseCallback {
            override fun onSuccess() {
                if (deleteOldVideo) {
                    deleteOldVideo(
                        oldVideoUrl,
                        deleteOldVideoImage,
                        oldVideoImageUrl,
                        video,
                        videoUri
                    )
                } else {
                    taskCompletedSuccessfully(video, videoUri)
                }
            }

            override fun onFailure(errorMessage: String) {
                taskFailed(errorMessage, video.title, videoUri)
            }
        })
    }

    private fun deleteOldVideo(
        oldVideoUrl: String,
        deleteOldVideoImage: Boolean,
        oldVideoImageUrl: String,
        video: Video,
        videoUri: Uri
    ) {
        firebaseController.deleteFileUsingUrl(
            oldVideoUrl,
            object : FirebaseController.FirebaseCallback {
                override fun onSuccess() {
                    if (deleteOldVideoImage) {
                        deleteOldVideoImage(oldVideoImageUrl, video, videoUri)
                    } else {
                        taskCompletedSuccessfully(video, videoUri)
                    }
                }

                override fun onFailure(errorMessage: String) {
                    taskFailed(errorMessage, video.title, videoUri)
                }
            })
    }

    private fun deleteOldVideoImage(oldVideoImageUrl: String, video: Video, videoUri: Uri) {
        firebaseController.deleteFileUsingUrl(
            oldVideoImageUrl,
            object : FirebaseController.FirebaseCallback {
                override fun onSuccess() {
                    taskCompletedSuccessfully(video, videoUri)
                }

                override fun onFailure(errorMessage: String) {
                    taskFailed(errorMessage, video.title, videoUri)
                }
            })
    }

    private fun taskCompletedSuccessfully(video: Video, videoUri: Uri?) {
        showUploadFinishedNotification(
            video.videoUrl,
            videoUri,
            getString(R.string.uploaded_successfully),
            video.title
        )
        taskCompleted()
    }

    private fun taskFailed(errorMessage: String?, videoTitle: String?, videoUri: Uri?) {
        showUploadFinishedNotification(null, videoUri, errorMessage, videoTitle)
        taskCompleted()
    }

    /**
     * Show a notification for a finished upload.
     */
    private fun showUploadFinishedNotification(
        downloadUrl: String?,
        fileUri: Uri?,
        caption: String?,
        title: String?
    ) {
        // Hide the progress notification
        dismissProgressNotification()

        // Make Intent to MainActivity
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_VIDEO_URI, fileUri)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val success = downloadUrl != null
        //        String caption = success ? getString(R.string.upload_success) : getString(R.string.upload_failure);
        showFinishedNotification(caption, title, intent, success)
    }

    /**
     * Broadcast finished upload (success or failure).
     * return true if a running receiver received the broadcast.
     */
    private fun broadcastUploadFinished(downloadUrl: String?, fileUri: Uri?) {
        val success = downloadUrl != null
        val action = if (success) UPLOAD_COMPLETED else UPLOAD_ERROR

        val broadcastIntent = Intent(action)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_VIDEO_URI, fileUri)

        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }

    companion object {
        private const val TAG = "MyUploadService"

        /**
         * Intent Actions
         */
        const val ACTION_UPLOAD: String = "action_upload"
        const val UPLOAD_COMPLETED: String = "upload_completed"
        const val UPLOAD_ERROR: String = "upload_error"

        /**
         * Intent Extras
         */
        const val EXTRA_VIDEO_URI: String = "extra_video_uri"
        const val EXTRA_VIDEO_NAME: String = "extra_video_name"
        const val EXTRA_VIDEO_MODEL: String = "extra_video_model"
        const val EXTRA_IS_OLD_VIDEO_IMAGE_DELETED: String = "is_old_video_image_deleted"
        const val EXTRA_IS_OLD_VIDEO_DELETED: String = "is_old_video_deleted"
        const val EXTRA_OLD_VIDEO_IMAGE_URL: String = "old_video_image_url"

        const val EXTRA_DOWNLOAD_URL: String = "extra_download_url"

        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(UPLOAD_COMPLETED)
                filter.addAction(UPLOAD_ERROR)

                return filter
            }
    }
}