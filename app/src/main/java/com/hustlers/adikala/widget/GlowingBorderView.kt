package com.hustlers.adikala.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class GlowingBorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val rectF = RectF()

    private var rotationAngle = 0f
    private var animator: ValueAnimator? = null

    // base stroke color -> bright highlight -> base, repeated so it reads as a moving glint
    private var cornerRadiusPx = dpToPx(20f)
    private var strokeWidthPx = dpToPx(3f)   // was 1.5f — now actually visible in the 3dp gap

    private var glowColors = intArrayOf(
        Color.parseColor("#E9BDB0"),
        Color.parseColor("#FF7A45"),   // warm copper glint, ties to button color
        Color.parseColor("#E9BDB0"),
        Color.parseColor("#E9BDB0"),
        Color.parseColor("#FF7A45"),
        Color.parseColor("#E9BDB0")
    )
    private var glowPositions = floatArrayOf(0f, 0.08f, 0.16f, 0.5f, 0.58f, 0.66f)

    init {
        paint.strokeWidth = strokeWidthPx
    }

    fun setStrokeWidthDp(dp: Float) {
        strokeWidthPx = dpToPx(dp)
        paint.strokeWidth = strokeWidthPx
        requestLayoutInsets()
    }

    fun setCornerRadiusDp(dp: Float) {
        cornerRadiusPx = dpToPx(dp)
        invalidate()
    }

    private fun dpToPx(dp: Float) = dp * resources.displayMetrics.density

    private fun requestLayoutInsets() {
        if (width > 0 && height > 0) {
            val inset = strokeWidthPx / 2f
            rectF.set(inset, inset, width - inset, height - inset)
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        requestLayoutInsets()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val shader = SweepGradient(width / 2f, height / 2f, glowColors, glowPositions)
        val matrix = Matrix()
        matrix.postRotate(rotationAngle, width / 2f, height / 2f)
        shader.setLocalMatrix(matrix)
        paint.shader = shader

        canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)
    }

    fun startGlowAnimation() {
        stopGlowAnimation()
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2400L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                rotationAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopGlowAnimation() {
        animator?.cancel()
        animator = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGlowAnimation()
    }
}