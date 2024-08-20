package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarCodeAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_UPC_A
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var currentToast: Toast? = null

    private var lastToast: Long = 0
    private val toastInterval: Long = 5000

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
                            currentToast = Toast.makeText(context, result, Toast.LENGTH_LONG)
                            currentToast?.show()
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