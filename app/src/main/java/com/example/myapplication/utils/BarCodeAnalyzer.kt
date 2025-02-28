package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.example.myapplication.data.Record
import com.example.myapplication.ui.createRecordDetailDialog

class BarCodeAnalyzer(
    private val context: Context,
    private val onLoading: (Boolean) -> Unit,
    private val onBarCodeScanned: () -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var lastScan: Long = 0
    private val scanInterval: Long = 5000

    private val recordSearch = RecordSearch()
    private var isToastShown = false
    private var isDialogOpen = false // Flag to prevent scanning while dialog is open

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (isDialogOpen) { // Prevent scanning if a dialog is open
            imageProxy.close()
            return
        }

        imageProxy.image?.let { image ->
            scanner.process(
                InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            ).addOnSuccessListener { barcode ->
                if (barcode.isNotEmpty()) {
                    val result = barcode.mapNotNull {
                        it.rawValue ?: it.displayValue ?: it.url?.url
                    }.joinToString(",")

                    if (result.isNotEmpty()) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastScan > scanInterval) {
                            lastScan = currentTime
                            onLoading(true)
                            onBarCodeScanned()

                            recordSearch.searchByBarcode(
                                context, result
                            ) { record, year, country, format, label, genre, style, cover, lowestPrice, numForSale, url, averageRating, ratingCount, artistImage ->

                                if (record != "No releases found") {
                                    val links = url?.let { extractLinks(it) }
                                    val albumLink = links?.second
                                    val artistLink = links?.first

                                    val message = buildString {
                                        append("Released $year - $country<br><br>")
                                        append("$format<br><br>")
                                        append("Label: $label")
                                    }

                                    val newRecord = Record(
                                        title = record,
                                        year = year.toString(),
                                        country = country.toString(),
                                        format = format.toString(),
                                        label = label.toString(),
                                        genre = genre.toString(),
                                        style = style.toString(),
                                        cover = cover.toString(),
                                        lowestPrice = lowestPrice.toString(),
                                        numForSale = numForSale.toString(),
                                        spotifyLink = albumLink ?: "",
                                        timestamp = System.currentTimeMillis(),
                                        averageRating = averageRating ?: "",
                                        ratingCount = ratingCount ?: "",
                                        artistImage = artistImage ?: "",
                                        artistLink = artistLink ?: ""
                                    )

                                    isDialogOpen = true // Disable scanning when dialog opens

                                    val alertDialog = createRecordDetailDialog(
                                        context = context,
                                        record = newRecord,
                                        message = message,
                                        onDismiss = {
                                            isDialogOpen =
                                                false // Re-enable scanning when dialog closes
                                        },
                                        onSave = { isDialogOpen = false }
                                    )

                                    onLoading(false)
                                    alertDialog.show()
                                } else if (!isToastShown) {
                                    Toast.makeText(context, "No releases found", Toast.LENGTH_SHORT).show()
                                    isToastShown = true
                                }
                                lastScan = currentTime
                                onLoading(false)
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                Log.e("BarCodeAnalyzer", "Barcode processing failed: ${it.localizedMessage}", it)
                onLoading(false)
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}
