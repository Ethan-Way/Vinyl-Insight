package com.example.myapplication.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun createCustomMarkerIcon(context: Context, rating: Number): BitmapDescriptor {
    val iconDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.marker_icon)!!
    val width = 150
    val height = 80
    val imageSize = 60
    val cornerRadius = 20f
    val triangleHeight = 20
    val bitmap = Bitmap.createBitmap(width, height + triangleHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val rectPaint = Paint().apply {
        color = colorResource(id = R.color.background).toArgb()
        style = Paint.Style.FILL
    }
    val rectPath = Path().apply {
        addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, Path.Direction.CW)
    }
    canvas.drawPath(rectPath, rectPaint)

    val trianglePath = Path().apply {
        moveTo(width / 2f, height.toFloat())
        lineTo((width / 2f) - 10, height + triangleHeight.toFloat())
        lineTo((width / 2f) + 10, height + triangleHeight.toFloat())
        close()
    }

    val matrix = Matrix().apply {
        postRotate(180f, width / 2f, height + triangleHeight / 2f)
    }
    trianglePath.transform(matrix)

    canvas.drawPath(trianglePath, rectPaint)

    val iconLeft = 10
    val iconTop = (height - imageSize) / 2
    val iconRight = iconLeft + imageSize
    val iconBottom = iconTop + imageSize
    iconDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom)
    iconDrawable.draw(canvas)

    val textPaint = Paint().apply {
        color = colorResource(id = R.color.primary_text).toArgb()
        textSize = 30f
        textAlign = Paint.Align.LEFT
        isAntiAlias = true
    }
    val ratingText = String.format("%.1f", rating)
    val textBounds = Rect()
    textPaint.getTextBounds(ratingText, 0, ratingText.length, textBounds)
    canvas.drawText(ratingText, (iconRight + 10).toFloat(), (height / 2 + textBounds.height() / 2).toFloat(), textPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
