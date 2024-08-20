package com.example.myapplication

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@ExperimentalPermissionsApi
@Composable
fun MainScreen() {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    
    if (cameraPermission.status.isGranted) {
        CameraScreen()
    } else if (cameraPermission.status.shouldShowRationale) {
        Text(text = "Camera permission not granted")
    } else {
        SideEffect {
            cameraPermission.run { launchPermissionRequest() }
        }
        Text(text = "No Camera Permission")
    }
}