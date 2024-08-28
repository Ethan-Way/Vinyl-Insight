package com.example.myapplication.ui

import android.content.DialogInterface
import androidx.compose.material3.Icon
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Record
import com.example.myapplication.data.RecordDao
import com.example.myapplication.utils.parseRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val recordDao: RecordDao = db.recordDao()
    var records by remember { mutableStateOf<List<Record>>(emptyList()) }
    var selectedRecord by remember { mutableStateOf<Pair<Record?, Long>>(null to 0L) }
    var selectedFilter by remember { mutableStateOf("AlphabeticalArtist") }
    var showDialog by remember { mutableStateOf(false) }

    // Load records from the database
    LaunchedEffect(Unit) {
        records = recordDao.getAllRecords()
    }

    // Filter and sort records based on the selected filter
    val sortedRecords = when (selectedFilter) {
        "AlphabeticalArtist" -> records.sortedBy { parseRecord(it.title)?.first }
        "AlphabeticalAlbum" -> records.sortedBy { parseRecord(it.title)?.second }
        "NewestTimestamp" -> records.sortedByDescending { it.timestamp }
        "OldestTimestamp" -> records.sortedBy { it.timestamp }
        else -> records
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(75, 75, 75))
    ) {
        item {
            TopAppBar(
                title = { Text("Saved Records") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .width(80.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier
                                .size(40.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(51, 51, 51),
                    titleContentColor = Color.White
                )
            )
        }

        when (selectedFilter) {
            "AlphabeticalArtist" -> {
                val groupedRecords = sortedRecords.groupBy {
                    parseRecord(it.title)?.first?.firstOrNull()?.uppercaseChar() ?: '?'
                }
                groupedRecords.forEach { (letter, records) ->
                    if (letter != '?') {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = letter.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    records.forEach { record ->
                        item {
                            RecordItem(record = record, onClick = {
                                selectedRecord = record to System.currentTimeMillis()
                            })
                        }
                    }
                }
            }

            "AlphabeticalAlbum" -> {
                val groupedRecords = sortedRecords.groupBy {
                    parseRecord(it.title)?.second?.firstOrNull()?.uppercaseChar() ?: '?'
                }
                groupedRecords.forEach { (letter, records) ->
                    if (letter != '?') {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = letter.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    records.forEach { record ->
                        item {
                            RecordItem(record = record, onClick = {
                                selectedRecord = record to System.currentTimeMillis()
                            })
                        }
                    }
                }
            }

            "NewestTimestamp", "OldestTimestamp" -> {
                val groupedRecords = sortedRecords.groupBy { record ->
                    java.text.SimpleDateFormat("MMM dd, yyyy").format(record.timestamp)
                }

                groupedRecords.forEach { (date, records) ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Text(
                                text = date,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    records.forEach { record ->
                        item {
                            RecordItem(record = record, onClick = {
                                selectedRecord = record to System.currentTimeMillis()
                            })
                        }
                    }
                }
            }
        }
    }

    selectedRecord.first?.let { record ->
        RecordDetailDialog(
            record = record,
            onDismiss = { selectedRecord = null to 0L },
            onDelete = {
                CoroutineScope(Dispatchers.IO).launch {
                    val updatedRecords = recordDao.getAllRecords()
                    withContext(Dispatchers.Main) {
                        records = updatedRecords
                    }
                }
            }
        )
    }

    CustomDialog(showDialog = showDialog, onDismiss = { showDialog = false },
        onSelectFilter = { filter ->
            selectedFilter = filter
            showDialog = false
        })
}

@Composable
fun RecordItem(record: Record, onClick: () -> Unit) {
    val result = parseRecord(record.title)
    if (result != null) {
        val (artist, album) = result

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
                .clickable { onClick() }
                .background(color = Color(51, 51, 51))
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = record.cover),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(horizontal = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)

            ) {
                Text(
                    text = album,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = artist,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Format: " + record.format,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Released: " + record.year,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun RecordDetailDialog(record: Record, onDismiss: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val recordDao: RecordDao = db.recordDao()

    val message = buildString {
        append("<b>${record.title}</b><br><br>")
        append("Released ${record.year} - ${record.country}<br><br>")
        append("${record.format}<br><br>")
        append("Label: ${record.label}<br><br>")
        append("Genre: ${record.genre}<br>")
        append("Style: ${record.style}<br><br>")
        append("${record.numForSale} copies listed, starting at ${record.lowestPrice}<br><br>")
        append("<a href='${record.spotifyLink}'>Spotify</a>")
    }

    val dialogView = View.inflate(context, R.layout.dialog_layout, null)
    val imageView = dialogView.findViewById<ImageView>(R.id.dialog_image)
    val messageView = dialogView.findViewById<TextView>(R.id.dialog_message)

    Glide.with(context)
        .load(record.cover)
        .apply(RequestOptions().centerCrop())
        .into(imageView)

    messageView.text =
        Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
    messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
    messageView.movementMethod = LinkMovementMethod.getInstance()

    val alertDialog =
        android.app.AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                onDismiss()
            }
            .setNegativeButton("Remove") { dialog: DialogInterface, _: Int ->
                CoroutineScope(Dispatchers.IO).launch {
                    recordDao.delete(record)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Record removed",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDelete()
                        dialog.dismiss()
                        onDismiss()
                    }
                }
            }
            .create()

    alertDialog.setOnShowListener {
        alertDialog.window?.setBackgroundDrawable(
            ColorDrawable(
                android.graphics.Color.TRANSPARENT
            )
        )

        val positiveButton =
            alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.white
            )
        )
        val negativeButton =
            alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.white
            )
        )

    }
    alertDialog.show()
}
