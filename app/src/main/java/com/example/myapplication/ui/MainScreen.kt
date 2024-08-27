package com.example.myapplication.ui

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@ExperimentalPermissionsApi
@Composable
fun MainScreen(navController: NavController) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    if (cameraPermission.status.isGranted) {
        CameraScreen(navController)
    } else if (cameraPermission.status.shouldShowRationale) {
        Text(text = "Camera permission not granted")
    } else {
        SideEffect {
            cameraPermission.run { launchPermissionRequest() }
        }
        Text(text = "No Camera Permission")
    }
}