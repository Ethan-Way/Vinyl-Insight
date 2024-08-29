package com.example.myapplication.utils

import android.widget.ImageView
import com.example.myapplication.R
import java.math.BigDecimal

fun setRatingStars(stars: Array<ImageView>, averageRating: BigDecimal?) {
    val fullStars = (averageRating ?: BigDecimal.ZERO).toInt()
    val remainder = (averageRating ?: BigDecimal.ZERO).remainder(BigDecimal.ONE)
    val hasHalfStar = remainder >= BigDecimal("0.3")

    for (i in stars.indices) {
        stars[i].setImageResource(
            when {
                i < fullStars -> R.drawable.star_full
                i == fullStars && hasHalfStar -> R.drawable.star_half
                else -> R.drawable.star_empty
            }
        )
    }
}
