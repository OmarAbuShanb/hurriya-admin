package dev.anonymous.hurriya.admin.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.anonymous.hurriya.admin.R

/**
 * Base class for Services that keep track of the number of active jobs and self-stop when the
 * count is zero.
 */
abstract class BaseTaskService : Service() {
    var numTasks: Int = 0
        private set

    fun taskStarted() {
        changeNumberOfTasks(1)
    }

    fun taskCompleted() {
        changeNumberOfTasks(-1)
    }

    @Synchronized
    private fun changeNumberOfTasks(delta: Int) {
        Log.d(TAG, "changeNumberOfTasks:" + this.numTasks + ":" + delta)
        this.numTasks += delta

        // If there are no tasks left, stop the service
        if (this.numTasks <= 0) {
            Log.d(TAG, "stopping")
            stopSelf()
        }
    }

    private fun createDefaultChannel() {
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                "Default",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
    }

    protected fun startTaskForeground(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(id, notification)
        }
    }

    /**
     * Show notification with a progress bar.
     */
    protected fun progressNotification(title: String?, percentComplete: Int): Notification {
        var title = title
        createDefaultChannel()

        val indeterminate = this.numTasks > 1

        val caption: String?
        if (this.numTasks > 1) {
            title = getString(R.string.app_name)

            caption =
                getString(R.string.uploading) + " " + this.numTasks.toString() + " " + getString(R.string.video)
        } else {
            caption = getString(R.string.progress_uploading)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.ic_file_upload_white_24dp)
            .setContentTitle(title)
            .setContentText(caption)
            .setProgress(100, percentComplete, indeterminate)
            .setOngoing(true)
            .setAutoCancel(false)

        return builder.build()
    }

    /**
     * Show notification that the activity finished.
     */
    protected fun showFinishedNotification(
        caption: String?,
        title: String?,
        intent: Intent?,
        success: Boolean
    ) {
        // Make PendingIntent for notification
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            /* requestCode */intent,
            PendingIntent.FLAG_IMMUTABLE)

        val icon = if (success) R.drawable.ic_check_white_24 else R.drawable.ic_error_white_24dp

        createDefaultChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(caption)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(FINISHED_NOTIFICATION_ID, builder.build())
    }

    /**
     * Dismiss the progress notification.
     */
    protected fun dismissProgressNotification() {
        val manager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        manager.cancel(PROGRESS_NOTIFICATION_ID)
    }

    companion object {
        private const val CHANNEL_ID_DEFAULT = "default"

        const val PROGRESS_NOTIFICATION_ID: Int = 1
        const val FINISHED_NOTIFICATION_ID: Int = 2

        private const val TAG = "MyBaseTaskService"
    }
}