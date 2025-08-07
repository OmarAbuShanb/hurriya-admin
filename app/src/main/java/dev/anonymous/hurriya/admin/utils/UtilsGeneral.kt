package dev.anonymous.hurriya.admin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Size
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import dev.anonymous.hurriya.admin.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.concurrent.Volatile

class UtilsGeneral private constructor() {
    fun getFileName(uri: Uri, context: Context): String? {
        var result: String? = null

        val scheme = uri.scheme
        println("scheme = $scheme")
        if (scheme != null && scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        println("DISPLAY_NAME = $result")
                    }
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }

        if (result == null) {
            val uriPath = uri.path
            println("uriPath = $uriPath")
            if (uriPath != null) {
                val cut = uriPath.lastIndexOf('/')
                println("cut = $cut")
                if (cut != -1) {
                    result = uriPath.substring(cut + 1)
                    println("uriPath.substring(cut + 1 = $result")
                }
            }
        }

        if (result == null) {
            result = UUID.randomUUID().toString()
        }

        return result
    }

    fun getStringDateFromDate(date: Date): String {
        val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return format.format(date)
    }

    fun loadImage(context: Context, link: String): RequestBuilder<Drawable?> {
        return Glide
            .with(context)
            .load(link)
            .placeholder(R.color.place_holder_color)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
    }

    // SnackBar
    fun showSnackBar(view: View, text: String) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
    }

    // Toast
    fun showToast(context: Context?, text: String?) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    companion object {
        @get:Synchronized
        @Volatile
        var instance: UtilsGeneral? = null
            get() {
                if (field == null) {
                    field = UtilsGeneral()
                }
                return field!!
            }
            private set

        fun getThumbnailVideo(context: Context, contentUri: Uri): Bitmap? {
            var thumbnail: Bitmap? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    thumbnail = context.contentResolver.loadThumbnail(
                        contentUri,
                        Size(300, 300),
                        null
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            return thumbnail
        }
    }
}