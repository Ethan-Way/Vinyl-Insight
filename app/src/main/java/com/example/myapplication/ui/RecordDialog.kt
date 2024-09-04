package com.example.myapplication.ui

import android.content.Context
import android.view.View
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    Log.d("record", record.artistImage)

    val stars = arrayOf(
        dialogView.findViewById<ImageView>(R.id.star1),
        dialogView.findViewById<ImageView>(R.id.star2),
        dialogView.findViewById<ImageView>(R.id.star3),
        dialogView.findViewById<ImageView>(R.id.star4),
        dialogView.findViewById<ImageView>(R.id.star5)
    )

    val result = parseRecord(record.title)
    if (result != null) {
        val (artist, album) = result
        titleView.text = album
        artistView.text = artist
    }

    setRatingStars(stars, record.averageRating.toBigDecimal())

    Glide.with(context)
        .load(record.artistImage)
        .circleCrop()
        .into(artistImageView)

    Glide.with(context)
        .load(record.cover)
        .apply(RequestOptions().centerCrop())
        .into(imageView)

    messageView.text = Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
    messageView.movementMethod = LinkMovementMethod.getInstance()
    ratingTextView.text = "Rated ${record.averageRating} by ${record.ratingCount} users"

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
                setNegativeButton("Save") { _, _ ->
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
                setNegativeButton("Remove") { dialog: DialogInterface, _: Int ->
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

        val positiveButton =
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.white
            )
        )
        val negativeButton =
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.white
            )
        )
    }
    return alertDialog
}
