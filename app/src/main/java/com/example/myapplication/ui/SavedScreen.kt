package com.example.myapplication.ui

import android.app.Activity
import androidx.compose.material3.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Record
import com.example.myapplication.data.RecordDao
import com.example.myapplication.utils.parseRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current

    LaunchedEffect(Unit) {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                window.statusBarColor = ContextCompat.getColor(context, R.color.background)
            }
        }
    }

    val db = remember { AppDatabase.getDatabase(context) }
    val recordDao: RecordDao = db.recordDao()
    var records by remember { mutableStateOf<List<Record>>(emptyList()) }
    var selectedRecord by remember { mutableStateOf<Pair<Record?, Long>>(null to 0L) }
    var selectedFilter by remember { mutableStateOf("AlphabeticalArtist") }
    var showDialog by remember { mutableStateOf(false) }
    var isClickable by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

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
            .background(color = colorResource(id = R.color.bubble))
    ) {
        item {
            TopAppBar(
                title = { Text("Saved Records") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.primary_text)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (isClickable) {
                                isClickable = false
                                showDialog = true

                                scope.launch {
                                    delay(1000)
                                    isClickable = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = colorResource(id = R.color.primary_text)
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
                    containerColor = colorResource(id = R.color.background),
                    titleContentColor = colorResource(id = R.color.primary_text)
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
                                    color = colorResource(id = R.color.primary_text),
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
                                    color = colorResource(id = R.color.primary_text),
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
                                color = colorResource(id = R.color.primary_text),
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
                .background(color = colorResource(id = R.color.background))
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
                    color = colorResource(id = R.color.primary_text),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = artist,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary_text),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Format: " + record.format,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary_text),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Released: " + record.year,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary_text),
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

    val message = buildString {
        append("Released ${record.year} - ${record.country}<br><br>")
        append("${record.format}<br><br>")
        append("Label: ${record.label}")
    }

    val alertDialog = createRecordDetailDialog(
        context = context,
        record = record,
        message = message,
        onDismiss = onDismiss,
        onDelete = onDelete
    )

    alertDialog.show()
}
