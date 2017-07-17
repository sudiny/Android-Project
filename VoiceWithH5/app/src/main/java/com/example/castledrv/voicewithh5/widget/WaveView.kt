package com.example.castledrv.voicewithh5.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

import com.example.castledrv.voicewithh5.NewRecordTimesActivity

import java.util.ArrayList


/**
 * Created by Administrator on 2015/11/9.
 */
class WaveView : View {
    private val mRect = Rect()
    private val mForePaint = Paint()
    private val p = Paint()
    internal val column_space_dip = 4
    internal val column_width_dip = 4


    var mSpectrumNum = 0
    internal var h = 0
    internal var w = 0
    internal var column_space = 10
    internal var column_width = 10
    internal var column_maxTop = 90
    internal var default_columnH = 0
    internal var list_value: MutableList<Int> = ArrayList()
    private var isRight = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mForePaint.strokeWidth = 1f
        mForePaint.isAntiAlias = true
        mForePaint.color = Color.rgb(0, 184, 238)

        val density = resources.displayMetrics.density
        column_space = (density * column_space_dip).toInt()
        column_width = (density * column_width_dip).toInt()
        //			column_width=column_space;
        mSpectrumNum = w / (column_space + column_width)
        default_columnH = h / 10
        if (default_columnH == 0) {
            default_columnH = 1
        }
        column_maxTop = h
        for (i in 0..mSpectrumNum - 1) {
            list_value.add(h / NewRecordTimesActivity.maxValue)
        }
        p.color = Color.WHITE

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /**
         * 设置宽度
         */
        var specMode = View.MeasureSpec.getMode(widthMeasureSpec)
        var specSize = View.MeasureSpec.getSize(widthMeasureSpec)
        if (specMode == View.MeasureSpec.EXACTLY)
        // match_parent , accurate
        {
            w = specSize
        } else {
            //
        }
        /***
         * 设置高度
         */
        specMode = View.MeasureSpec.getMode(heightMeasureSpec)
        specSize = View.MeasureSpec.getSize(heightMeasureSpec)
        if (specMode == View.MeasureSpec.EXACTLY)
        // match_parent , accurate
        {
            h = specSize
        } else {
            //
        }
        setMeasuredDimension(w.toInt(), h.toInt())
        init()
    }

    fun updateData(list_row: List<Int>, max: Int) {
        val list_real = ArrayList<Int>()
        for (value in list_row) {
            val v = value * h / max
            list_real.add(v)
        }
        list_value = list_real
        invalidate()
    }

    fun setIsRight(isRight: Boolean) {
        this.isRight = isRight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (h == 0) {
            h = height
            w = width
            if (h == 0) {
                return
            }
        }
        mRect.set(0, 0, w, h)

        canvas.drawRect(mRect, p)
        //绘制频谱
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0
        if (isRight) {
            for (i in 0..mSpectrumNum - 1) {
                var value = list_value[i]
                if (value < 1) {
                    value = 1
                }
                left = i * column_width + i * column_space
                top = (column_maxTop - value) / 2
                right = left + column_width
                bottom = top + value

                //					canvas.drawRect(left, top, right, column_maxTop, mForePaint);
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mForePaint)
            }
        } else {
            for (i in 0..mSpectrumNum - 1) {
                var value = list_value[mSpectrumNum - 1 - i]
                if (value < 1) {
                    value = 1
                }
                left = i * column_width + (i + 1) * column_space
                top = (column_maxTop - value) / 2
                right = left + column_width
                bottom = top + value

                //					canvas.drawRect(left, top, right, column_maxTop, mForePaint);
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mForePaint)
            }
        }


    }

    companion object {

        val VISUALIZER_HEIGHT_DIP = 40f
        val VISUALIZER_WIDTH_DIP = 120f
    }
}
