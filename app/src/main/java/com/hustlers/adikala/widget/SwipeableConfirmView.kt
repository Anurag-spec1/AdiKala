package com.hustlers.adikala.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.hustlers.adikala.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SwipeableConfirmView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var buttonText: String = "Swipe to Go"
        set(value) { field = value; label.text = value }

    var onSwipeComplete: (() -> Unit)? = null

    private val label: TextView
    private val circle: FrameLayout
    private var maxDrag = 0f
    private var dragX = 0f
    private var isCompleted = false
    private var downX = 0f
    private var downY = 0f
    private var startDragX = 0f
    private var circleSizePx = 0
    private var isDragging = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    init {
        clipChildren = false

        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 999f
            setColor(Color.parseColor("#80FFFFFF"))
            setStroke(dp(1f), Color.parseColor("#33FFFFFF"))
        }

        label = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#E6000000"))
            textSize = 15f
            text = buttonText
            typeface = androidx.core.content.res.ResourcesCompat.getFont(
                context, R.font.space_grotesk_semibold
            )
        }
        addView(label, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        circle = FrameLayout(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
            }
            elevation = dp(4f).toFloat()
        }
        val arrow = ImageView(context).apply {
            setImageResource(R.drawable.ic_arrow_right)
            setColorFilter(Color.BLACK)
        }
        circle.addView(arrow, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        })
        addView(circle)
    }

    private fun dp(value: Float) = (value * resources.displayMetrics.density).toInt()

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        circleSizePx = height - dp(10f)
        maxDrag = (width - circleSizePx - dp(10f)).toFloat()
        layoutCircle(dragX)
    }

    private fun layoutCircle(x: Float) {
        val size = circleSizePx
        val top = (height - size) / 2
        circle.layout(x.toInt() + dp(5f), top, x.toInt() + dp(5f) + size, top + size)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isCompleted) return true

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                startDragX = dragX
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - downX
                val deltaY = event.rawY - downY

                if (!isDragging) {
                    if (abs(deltaX) > touchSlop && abs(deltaX) > abs(deltaY)) {
                        isDragging = true
                    } else if (abs(deltaY) > touchSlop && abs(deltaY) > abs(deltaX)) {
                        parent?.requestDisallowInterceptTouchEvent(false)
                        return false
                    }
                }

                if (isDragging) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    dragX = max(0f, min(maxDrag, startDragX + deltaX))
                    layoutCircle(dragX)
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (isDragging && dragX > maxDrag * 0.75f) {
                    animateTo(maxDrag, 250) {
                        isCompleted = true
                        onSwipeComplete?.invoke()
                        postDelayed({
                            animateTo(0f, 300) { isCompleted = false }
                        }, 500)
                    }
                } else {
                    animateTo(0f, 200)
                }
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animateTo(target: Float, duration: Long, onEnd: (() -> Unit)? = null) {
        ValueAnimator.ofFloat(dragX, target).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                dragX = it.animatedValue as Float
                layoutCircle(dragX)
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onEnd?.invoke()
                }
            })
            start()
        }
    }
}