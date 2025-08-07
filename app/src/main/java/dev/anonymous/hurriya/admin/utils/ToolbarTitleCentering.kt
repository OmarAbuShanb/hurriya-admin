package dev.anonymous.hurriya.admin.utils

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import dev.anonymous.hurriya.admin.R

object ToolbarTitleCentering {
    private var currentTitle: CharSequence? = null

    fun initWith(toolbar: MaterialToolbar, applicationContext: Context) {
        toolbar.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val newTitle = toolbar.title
            val loginFragmentTitle = ContextCompat.getString(applicationContext, R.string.login)
            if (newTitle == loginFragmentTitle || newTitle != currentTitle) {
                currentTitle = newTitle
                centerTitle(toolbar)
            }
        }
    }

    private fun centerTitle(toolbar: MaterialToolbar) {
        val titleView = (0 until toolbar.childCount)
            .map { toolbar.getChildAt(it) }
            .filterIsInstance<TextView>()
            .firstOrNull { it.text?.toString() == toolbar.title } ?: return

        // 1080 - 511 => 569 / 2 => 284.5 - 165 => 19.5
        val desiredOffset = (toolbar.width - titleView.width) / 2 - titleView.left
        titleView.translationX = desiredOffset.toFloat()
    }
}

