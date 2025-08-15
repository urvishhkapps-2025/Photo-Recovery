package com.Blue.photorecovery.common

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.style.ReplacementSpan

class LinearGradientSpan(
    private val startColor: Int,
    private val endColor: Int
) : ReplacementSpan() {
    private var spanWidth: Int = 0
    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?) =
        paint.measureText(text, start, end).toInt().also { spanWidth = it }

    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        val old = paint.shader
        paint.shader = LinearGradient(
            x, 0f, x + spanWidth, 0f,
            intArrayOf(startColor, endColor), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        paint.isAntiAlias = true
        canvas.drawText(text, start, end, x, y.toFloat(), paint)
        paint.shader = old
    }
}