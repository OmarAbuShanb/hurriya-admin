package dev.anonymous.hurriya.admin.presentation.components.ui

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import dev.anonymous.hurriya.admin.R

class LoadingButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val button: MaterialButton
    private val progress: CircularProgressIndicator
    private var originalWidth = 0
    private var isLoading = false
    private var isStateSavingEnabled = true
    private var buttonText = ""

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LoadingButtonView)
        val buttonStyleResId = ta.getResourceId(
            R.styleable.LoadingButtonView_buttonStyle,
            com.google.android.material.R.style.Widget_Material3_Button
        )
        isStateSavingEnabled =
            ta.getBoolean(R.styleable.LoadingButtonView_isStateSavingEnabled, true)
        buttonText = ta.getString(R.styleable.LoadingButtonView_text) ?: ""

        val backgroundTint = ta.getColorStateList(R.styleable.LoadingButtonView_backgroundTint)
        val textColor = ta.getColorStateList(R.styleable.LoadingButtonView_android_textColor)
        val stateListAnimatorResId =
            ta.getResourceId(R.styleable.LoadingButtonView_stateListAnimator, 0)

        ta.recycle()

        val themedContext = ContextThemeWrapper(context, buttonStyleResId)
        LayoutInflater.from(themedContext).inflate(R.layout.view_loading_button, this, true)

        button = findViewById(R.id.btn)
        progress = findViewById(R.id.progress)

        button.text = buttonText
        button.post {
            progress.indicatorSize = (button.height * 0.85).toInt()
        }

        backgroundTint?.let { button.backgroundTintList = it }
        textColor?.let { button.setTextColor(it) }

        if (stateListAnimatorResId != 0) {
            button.stateListAnimator = AnimatorInflater.loadStateListAnimator(
                context,
                stateListAnimatorResId
            )
        }
    }


    fun setText(text: String) {
        if (!isLoading) {
            buttonText = text
            button.text = buttonText
        }
    }

    fun setOnButtonClickListener(listener: OnClickListener) {
        button.setOnClickListener {
            if (!isLoading) listener.onClick(it)
        }
    }

    fun showLoading() {
        isLoading = true
        post {
            originalWidth = button.width
            ValueAnimator.ofInt(button.width, button.height).apply {
                duration = 300
                addUpdateListener {
                    button.layoutParams = button.layoutParams.apply {
                        width = it.animatedValue as Int
                    }
                }
                doOnStart {
                    button.text = ""
                    progress.visibility = VISIBLE
                    progress.alpha = 0f
                    progress.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
            }.start()
        }
    }

    fun hideLoading() {
        if (!isLoading) return
        isLoading = false

        ValueAnimator.ofInt(button.width, originalWidth).apply {
            duration = 300
            addUpdateListener {
                button.layoutParams = button.layoutParams.apply {
                    width = it.animatedValue as Int
                }
            }
            doOnStart {
                progress.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        progress.visibility = INVISIBLE
                    }.start()
            }
            doOnEnd {
                button.text = buttonText
            }
        }.start()
    }

    fun restoreLoadingState(shouldBeLoading: Boolean) {
        if (shouldBeLoading) {
            post {
                originalWidth = width
                button.text = ""
                button.layoutParams.width = button.height
                progress.indicatorSize = (button.height * 0.85).toInt()
                progress.visibility = VISIBLE
                progress.alpha = 1f
                isLoading = true
            }
        }
    }

    fun isCurrentlyLoading(): Boolean = isLoading

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState() ?: BaseSavedState.EMPTY_STATE
        if (!isStateSavingEnabled) return superState
        return SavedState(superState).also { it.isLoading = this.isLoading }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        if (isStateSavingEnabled) restoreLoadingState(state.isLoading)
    }

    private class SavedState : BaseSavedState {
        var isLoading: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            isLoading = parcel.readByte().toInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeByte(if (isLoading) 1 else 0)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}
