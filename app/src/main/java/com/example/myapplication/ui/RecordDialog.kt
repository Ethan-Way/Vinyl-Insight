package com.example.myapplication.ui

import android.content.Context
import android.view.View
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Record
import com.example.myapplication.utils.parseRecord
import com.example.myapplication.utils.setRatingStars
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

fun createRecordDetailDialog(
    context: Context,
    record: Record,
    message: String,
    onDismiss: () -> Unit,
    onSave: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
): AlertDialog {
    val dialogView = View.inflate(context, R.layout.dialog_layout, null)
    val titleView = dialogView.findViewById<TextView>(R.id.dialog_title)
    val artistView = dialogView.findViewById<TextView>(R.id.artist)
    val imageView = dialogView.findViewById<ImageView>(R.id.dialog_image)
    val artistImageView = dialogView.findViewById<ImageView>(R.id.artist_image)
    val messageView = dialogView.findViewById<TextView>(R.id.dialog_message)
    val playButton = dialogView.findViewById<Button>(R.id.button_play)
    val ratingTextView = dialogView.findViewById<TextView>(R.id.rating_text)
    val priceTextView = dialogView.findViewById<TextView>(R.id.price_text)
    val styleContainer = dialogView.findViewById<FlexboxLayout>(R.id.style_container)


    record.style.split(", ").forEach { style ->
        val styleTextView = TextView(context).apply {
            text = style
            setPadding(16, 16, 16, 16)
            setTextColor(ContextCompat.getColor(context, R.color.primary_text))
            background = ContextCompat.getDrawable(context, R.drawable.rounded_style)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 16)
            }
        }
        styleContainer.addView(styleTextView)
    }


    val stars = arrayOf(
        dialogView.findViewById(R.id.star1),
        dialogView.findViewById(R.id.star2),
        dialogView.findViewById(R.id.star3),
        dialogView.findViewById(R.id.star4),
        dialogView.findViewById<ImageView>(R.id.star5)
    )

    val result = parseRecord(record.title)
    if (result != null) {
        val (artist, album) = result
        titleView.text = buildString { append("$album ") }
        artistView.text = artist
    }

    val rating = record.averageRating.takeIf { it.isNotEmpty() }?.toBigDecimalOrNull() ?: BigDecimal.ZERO
    setRatingStars(stars, rating)

    Glide.with(context)
        .load(record.artistImage)
        .circleCrop()
        .into(artistImageView)

    artistImageView.setOnClickListener {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(record.artistLink)
        context.startActivity(intent)
    }

    Glide.with(context)
        .load(record.cover)
        .apply(RequestOptions().centerCrop())
        .into(imageView)

    messageView.text = Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
    messageView.movementMethod = LinkMovementMethod.getInstance()
    ratingTextView.text = buildString { append("Rated ${record.averageRating} by ${record.ratingCount} users") }
    priceTextView.text = buildString { append("${record.numForSale} copies listed, starting at ${record.lowestPrice}") }

    playButton.setOnClickListener {
        record.spotifyLink.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            context.startActivity(intent)
        }
    }

    val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
        .setView(dialogView)
        .setPositiveButton("Close") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            onDismiss()
        }.apply {
            onSave?.let {
                setNeutralButton("Save") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(context)
                        val recordDao = db.recordDao()

                        val recordExists = recordDao.recordExists(record.title, record.year)
                        if (recordExists == 0) {
                            recordDao.insert(record)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Record saved", Toast.LENGTH_SHORT).show()
                                it() // Trigger the save callback
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Record already saved", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }

            onDelete?.let {
                setNeutralButton("Remove") { dialog: DialogInterface, _: Int ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(context)
                        val recordDao = db.recordDao()

                        recordDao.delete(record)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Record removed", Toast.LENGTH_SHORT).show()
                            it() // Trigger the delete callback
                            dialog.dismiss()
                            onDismiss()
                        }
                    }
                }
            }
        }.create()

    alertDialog.setOnShowListener {
        alertDialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )

        val window = alertDialog.window
        window?.let {
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            it.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            it.statusBarColor = Color.TRANSPARENT
        }

        val neutralButton =
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        neutralButton.setTextColor(
            ContextCompat.getColor(context, R.color.primary_text)
        )
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        neutralButton.setPadding(16, 16, 16, 16)


        val positiveButton =
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(
            ContextCompat.getColor(context, R.color.primary_text)
        )
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        positiveButton.setPadding(16, 16, 16, 16)

        positiveButton.background = ContextCompat.getDrawable(context, R.drawable.rounded_style)
        neutralButton.background = ContextCompat.getDrawable(context, R.drawable.rounded_style)

        positiveButton.isAllCaps = false
        neutralButton.isAllCaps = false

        positiveButton.typeface = Typeface.DEFAULT
        neutralButton.typeface = Typeface.DEFAULT

    }
    return alertDialog
}
