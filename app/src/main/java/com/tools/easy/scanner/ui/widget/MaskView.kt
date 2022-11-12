package com.tools.easy.scanner.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tools.easy.scanner.R
import org.jetbrains.anko.dip

/**
 *  description :
 */
class MaskView: View {
    private lateinit var paint: Paint
    private lateinit var bitmapPaint: Paint
    private lateinit var bitmap: Bitmap
    private var rect = Rect()
    private var view: View? = null
    private val xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    private var left = 0f
    private var top = 0f
    private var right = 0f
    private var bottom = 0f
    private var guideClickTop = 0f
    private var guideClickLeft = 0f
    private var onClick: (() -> Unit)? = null

    constructor(context: Context) : this(context, null) {
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint.color = Color.parseColor("#000000")
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        bitmapPaint = Paint()
        bitmapPaint.color = Color.WHITE
        bitmapPaint.style = Paint.Style.FILL
        bitmapPaint.isAntiAlias = true

        isClickable = true
        isFocusable = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        bitmap = BitmapFactory.decodeResource(resources, R.mipmap.click)
    }

    fun setView(view: View, onClick: () -> Unit) {
        this.view = view
        this.onClick = onClick
        view.post {
            left = view.x
            right = view.x + view.width
            top = view.y
            bottom = view.y + view.height
            guideClickLeft = (left + right) / 2.5f
            guideClickTop = (view.bottom - dip(50f)).toFloat()
            rect = Rect(0, 0, width, height)

            invalidate()
        }
    }

    private fun dp2px(context: Context, value: Int): Int {
        val v = context.resources.displayMetrics.density
        return (v * value + 0.5f).toInt()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.xfermode = xfermode
        paint.color = Color.TRANSPARENT
        canvas?.drawCircle((left+right)/2, (top+bottom)/2, dp2px(context, 100).toFloat(), paint)
        //canvas?.drawRect(left, top, right, bottom, paint)
        paint.xfermode = null

        canvas?.drawBitmap(bitmap, guideClickLeft, guideClickTop, bitmapPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                /*if (view == null) return true

                val location = IntArray(2)
                view?.getLocationOnScreen(location)
                if (event.rawX >= location[0] && event.rawX <= location[0] + view!!.width
                    && event.rawY >= location[1] && event.rawY <= location[1] + view!!.height
                ) {
                    onClick?.invoke()
                }*/
                onClick?.invoke()
            }
        }
        return super.onTouchEvent(event)
    }
}