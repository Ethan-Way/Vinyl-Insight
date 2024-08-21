package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable


class BarCodeAnalyzer(private val context: Context, private val rootView: View) :
    ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_ALL_FORMATS
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var currentToast: Toast? = null

    private var lastToast: Long = 0
    private val toastInterval: Long = 5000

    private val recordSearch = RecordSearch()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        imageProxy.image?.let { image ->
            scanner.process(
                InputImage.fromMediaImage(
                    image, imageProxy.imageInfo.rotationDegrees
                )
            ).addOnSuccessListener { barcode ->
                if (barcode.isNotEmpty()) {
                    val result = barcode.mapNotNull {
                        it.rawValue ?: it.displayValue ?: it.url?.url
                    }.joinToString(",")

                    if (result.isNotEmpty()) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastToast > toastInterval) {
                            currentToast?.cancel()
                            recordSearch.searchByBarcode(result) { record, year, format, label, style ->
                                val message = buildString {
                                    append("$record\n")
                                    append("$year\n")
                                    append("$format\n")
                                    append("$label\n")
                                    append("$style\n")
                                }

                                val alertDialog =
                                    AlertDialog.Builder(context, R.style.CustomAlertDialog)
                                        .setTitle("Record Found")
                                        .setMessage(message)
                                        .setPositiveButton("ok") { dialog: DialogInterface, _: Int ->
                                            dialog.dismiss()
                                        }
                                        .create()

                                alertDialog.setOnShowListener {
                                    alertDialog.window?.setBackgroundDrawable(
                                        ColorDrawable(
                                            Color.TRANSPARENT
                                        )
                                    )

                                }
                                alertDialog.show()
                            }
                            lastToast = currentTime

                        }
                    }
                }
            }.addOnFailureListener {
                Log.e("BarCodeAnalyzer", "Barcode processing failed: ${it.localizedMessage}", it)
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}