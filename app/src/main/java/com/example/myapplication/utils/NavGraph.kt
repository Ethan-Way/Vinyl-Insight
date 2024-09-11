package com.example.myapplication.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.MainScreen
import com.example.myapplication.ui.MapScreen
import com.example.myapplication.ui.SavedScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("savedScreen") { SavedScreen(navController) }
        composable("mapScreen") {MapScreen(navController)}
    }
}
