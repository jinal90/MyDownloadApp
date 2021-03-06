package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    //Size
    private var w = 0
    private var h = 0
    private var textWidth = 0f
    private var progressWidth = 0f
    private var progressCircle = 0f

    //colors
    private val buttonColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private val loadingColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private var circleColor = ContextCompat.getColor(context, R.color.colorAccent)

    //text to display on the button
    private var buttonText: String

    // paint object for draw
    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = resources.getDimension(R.dimen.button_text_size)
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        var valueAnimator = ValueAnimator()
        when (new) {
            ButtonState.Loading -> {
                circleColor = ContextCompat.getColor(context, R.color.colorAccent)
                buttonText = resources.getString(R.string.button_loading)
                valueAnimator = ValueAnimator.ofFloat(0f, w.toFloat())
                with(valueAnimator) {
                    duration = 4000
                    addUpdateListener { animation ->
                        progressWidth = animation.animatedValue as Float
                        progressCircle = (w.toFloat() / 365) * progressWidth
                        invalidate()
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            progressWidth = 0f
                            progressCircle = 0f
                            if (buttonState == ButtonState.Loading) {
                                buttonState = ButtonState.Loading
                            }
                        }
                    })
                    start()
                }
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                progressWidth = 0f
                progressCircle = 0f
                circleColor = buttonColor
                buttonText = getContext().getString(R.string.button_download)
                invalidate()
            }
            else -> {
                //do nothing
            }
        }
    }

    init {
        buttonText = getContext().getString(R.string.button_download)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            with(it) {

                paint.color = buttonColor
                drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
                paint.color = loadingColor
                drawRect(0f, 0f, progressWidth, h.toFloat(), paint)
                paint.color = Color.WHITE
                textWidth = paint.measureText(buttonText)
                drawText(
                    buttonText,
                    w / 2 - textWidth / 2,
                    h / 2 - (paint.descent() + paint.ascent()) / 2,
                    paint
                )
                save()
                val circleXOffset = paint.textSize / 2
                translate(w / 2 + textWidth / 2 + circleXOffset, h / 2 - paint.textSize / 2)
                paint.color = circleColor
                val oval = RectF(0f, 0f, paint.textSize, paint.textSize)
                drawArc(oval, 0F, progressCircle, true, paint)
                restore()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minimumWidth = paddingLeft + paddingRight + suggestedMinimumWidth
        w = resolveSizeAndState(minimumWidth, widthMeasureSpec, 1)
        h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
    }
}