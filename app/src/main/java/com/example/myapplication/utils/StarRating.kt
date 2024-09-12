package com.example.myapplication.utils

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

@Composable
fun StarRating(rating: Float, modifier: Modifier = Modifier) {
    val fullStars = rating.toInt()
    val hasHalfStar = (rating % 2) > 0
    val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0

    Row(modifier = modifier) {
        repeat(fullStars) {
            Image(
                painter = painterResource(id = R.drawable.star_full),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        }
        if (hasHalfStar) {
            Image(
                painter = painterResource(id = R.drawable.star_half),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        }
        repeat(emptyStars) {
            Image(
                painter = painterResource(id = R.drawable.star_empty),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}




