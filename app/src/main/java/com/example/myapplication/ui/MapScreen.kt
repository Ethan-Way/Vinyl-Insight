package com.example.myapplication.ui

import com.example.myapplication.BuildConfig
import android.Manifest
import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.utils.getUserLocation
import com.example.myapplication.utils.searchNearbyStores
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavController) {
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    if (!Places.isInitialized()) {
        Places.initialize(context, BuildConfig.googleMapsApiKey)
    }

    val placesClient = Places.createClient(context)
    var nearbyStores by remember {
        mutableStateOf<List<Place>>(emptyList())
    }

    val mapStyle = remember {
        val inputStream = context.resources.openRawResource(R.raw.dark_mode_style)
        inputStream.bufferedReader().use { it.readText() }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
                location?.let {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude), 14f
                            )
                        )
                    }
                    searchNearbyStores(placesClient, LatLng(it.latitude, it.longitude)) { places ->
                        Log.d("places", places.toString())
                        nearbyStores = places
                    }
                }
            }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.bubble))
    ) {
        Column {
            TopAppBar(
                title = { Text("Store Locator") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.primary_text)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.background),
                    titleContentColor = colorResource(id = R.color.primary_text)
                ),
            )

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = true
                ),
                properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted, mapStyleOptions = MapStyleOptions(mapStyle))
            ) {
                nearbyStores.forEach { store ->
                    store.latLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = store.name,
                            snippet = store.address
                        )
                    }
                }
            }
        }
    }
}