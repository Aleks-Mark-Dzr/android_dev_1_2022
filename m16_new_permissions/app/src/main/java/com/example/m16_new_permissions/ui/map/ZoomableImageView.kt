package com.example.m16_new_permissions.ui.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatImageView

/** Увеличение фотографии относительно точки между пальцами и перемещение в её границах. */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var zoom = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var downY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var moved = false
    private var multiTouch = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val photoBounds = RectF()
    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val nextZoom = (zoom * detector.scaleFactor).coerceIn(1f, 5f)
                val ratio = nextZoom / zoom
                offsetX = detector.focusX - (detector.focusX - offsetX) * ratio
                offsetY = detector.focusY - (detector.focusY - offsetY) * ratio
                zoom = nextZoom
                constrainOffsets()
                invalidate()
                return true
            }
        }
    )

    fun resetZoom() {
        zoom = 1f
        offsetX = 0f
        offsetY = 0f
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetZoom()
    }

    override fun onDraw(canvas: Canvas) {
        val checkpoint = canvas.save()
        canvas.clipRect(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        canvas.translate(offsetX, offsetY)
        canvas.scale(zoom, zoom)
        super.onDraw(canvas)
        canvas.restoreToCount(checkpoint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || drawable == null) return super.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                activePointerId = event.getPointerId(0)
                downX = event.x
                downY = event.y
                lastX = event.x
                lastY = event.y
                moved = false
                multiTouch = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> multiTouch = true
            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(activePointerId)
                if (index >= 0) {
                    val x = event.getX(index)
                    val y = event.getY(index)
                    if (!moved) {
                        val dx = x - downX
                        val dy = y - downY
                        moved = dx * dx + dy * dy > touchSlop * touchSlop
                    }
                    if (event.pointerCount == 1 && !scaleDetector.isInProgress && moved) {
                        offsetX += x - lastX
                        offsetY += y - lastY
                        constrainOffsets()
                        invalidate()
                    }
                    lastX = x
                    lastY = y
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == activePointerId) {
                    val remainingIndex = if (event.actionIndex == 0) 1 else 0
                    activePointerId = event.getPointerId(remainingIndex)
                    lastX = event.getX(remainingIndex)
                    lastY = event.getY(remainingIndex)
                }
            }
            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                activePointerId = MotionEvent.INVALID_POINTER_ID
                // Ни pinch, ни перетаскивание не должны переключать фотографию.
                if (!moved && !multiTouch) performClick()
            }
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()

    private fun constrainOffsets() {
        val photo = drawable ?: return
        photoBounds.set(photo.bounds)
        imageMatrix.mapRect(photoBounds)
        photoBounds.offset(paddingLeft.toFloat(), paddingTop.toFloat())
        offsetX = constrainOffset(
            offsetX, photoBounds.left, photoBounds.right,
            paddingLeft.toFloat(), (width - paddingRight).toFloat()
        )
        offsetY = constrainOffset(
            offsetY, photoBounds.top, photoBounds.bottom,
            paddingTop.toFloat(), (height - paddingBottom).toFloat()
        )
    }

    private fun constrainOffset(
        offset: Float, photoStart: Float, photoEnd: Float,
        viewportStart: Float, viewportEnd: Float
    ): Float {
        return if ((photoEnd - photoStart) * zoom <= viewportEnd - viewportStart) {
            (viewportStart + viewportEnd - (photoStart + photoEnd) * zoom) / 2f
        } else {
            offset.coerceIn(viewportEnd - photoEnd * zoom, viewportStart - photoStart * zoom)
        }
    }
}
