package dev.anonymous.hurriya.admin

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureNanoTime

@RunWith(AndroidJUnit4::class)
class LayoutPerformanceTest {

    private val layoutInflater by lazy { LayoutInflater.from(getInstrumentation().targetContext) }

    @Test
    fun compareLayouts() {
        val result = measureAverageInflationAndDrawTime(
            R.layout.item_video_thumbnail_constraint,
            R.layout.item_video_thumbnail_frame,
        )

        val (constraintLayoutTime, constraintDrawTime) = result.first
        val (frameLayoutTime, frameDrawTime) = result.second

        Log.i(
            TAG,
            "$CONSTRAINT_LAYOUT_TAG Average inflate+measure+layout time (ns): $constraintLayoutTime"
        )
        Log.i(TAG, "$CONSTRAINT_LAYOUT_TAG Average draw time (ns): $constraintDrawTime")

        Log.i(TAG, "$FRAME_LAYOUT_TAG Average inflate+measure+layout time (ns): $frameLayoutTime")
        Log.i(TAG, "$FRAME_LAYOUT_TAG Average draw time (ns): $frameDrawTime")
    }

    private fun measureAverageInflationAndDrawTime(
        @LayoutRes firstLayoutRes: Int,
        @LayoutRes secondLayoutRes: Int,
    ): Pair<Pair<Long, Long>, Pair<Long, Long>> {
        var totalFirstLayoutTime = 0L
        var totalFirstDrawTime = 0L
        var totalSecondLayoutTime = 0L
        var totalSecondDrawTime = 0L

        val inflationPerLayout = INFLATION_COUNT / INFLATION_PER_MEASUREMENT

        repeat(inflationPerLayout) {
            val (layoutTime1, drawTime1) = measureInflationMeasureAndDraw(firstLayoutRes)
            val (layoutTime2, drawTime2) = measureInflationMeasureAndDraw(secondLayoutRes)

            totalFirstLayoutTime += layoutTime1
            totalFirstDrawTime += drawTime1

            totalSecondLayoutTime += layoutTime2
            totalSecondDrawTime += drawTime2
        }

        return (
                totalFirstLayoutTime / inflationPerLayout
                        to totalFirstDrawTime / inflationPerLayout
                ) to (
                totalSecondLayoutTime / inflationPerLayout
                        to totalSecondDrawTime / inflationPerLayout
                )
    }

    private fun measureInflationMeasureAndDraw(@LayoutRes layoutRes: Int): Pair<Long, Long> {
        var totalLayoutTimeNs = 0L
        var totalDrawTimeNs = 0L

        repeat(INFLATION_PER_MEASUREMENT) {
            val view = layoutInflater.inflate(layoutRes, null).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            totalLayoutTimeNs += measureNanoTime {
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            }

            val bitmap = Bitmap.createBitmap(
                view.measuredWidth.takeIf { it > 0 } ?: 1,
                view.measuredHeight.takeIf { it > 0 } ?: 1,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)

            totalDrawTimeNs += measureNanoTime {
                view.draw(canvas)
            }
        }

        val avgLayoutTimeNs = totalLayoutTimeNs / INFLATION_PER_MEASUREMENT
        val avgDrawTimeNs = totalDrawTimeNs / INFLATION_PER_MEASUREMENT

        return avgLayoutTimeNs to avgDrawTimeNs
    }

    companion object {
        private const val TAG = "LayoutPerformanceTest"

        private const val INFLATION_COUNT = 1000
        private const val INFLATION_PER_MEASUREMENT = 10

        private const val CONSTRAINT_LAYOUT_TAG = "ConstraintLayout:"
        private const val FRAME_LAYOUT_TAG = "FrameLayout:"
    }
}
