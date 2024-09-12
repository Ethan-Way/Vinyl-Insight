package com.example.myapplication.ui

import com.example.myapplication.BuildConfig
import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.utils.StarRating
import com.example.myapplication.utils.isStoreOpen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun MapScreen(navController: NavController) {
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    if (!Places.isInitialized()) {
        Places.initialize(context, BuildConfig.googleMapsApiKey)
    }

    val placesClient = Places.createClient(context)
    var nearbyStores by remember { mutableStateOf<List<Place>>(emptyList()) }
    var selectedStore by remember { mutableStateOf<Place?>(null) }

    val mapStyle = remember {
        val inputStream = context.resources.openRawResource(R.raw.map_style)
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
                    myLocationButtonEnabled = false
                ),
                properties = MapProperties(
                    isMyLocationEnabled = locationPermission.status.isGranted,
                    mapStyleOptions = MapStyleOptions(mapStyle)
                ),
            ) {
                nearbyStores.forEach { store ->
                    store.latLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = store.name,
                            snippet = store.address,
                            onClick = {
                                selectedStore = store
                                showBottomSheet = true
                                true
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                userLocation?.let { location ->
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude), 14f
                            )
                        )
                    }
                }
            },
            containerColor = colorResource(id = R.color.background),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .offset(y = 105.dp)
                .width(70.dp)
                .height(70.dp)
                .padding(top = 20.dp, start = 20.dp),
            shape = RoundedCornerShape(50.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "My Location",
                modifier = Modifier
                    .size(25.dp),
                tint = colorResource(id = R.color.primary_text)
            )
        }
    }

    if (showBottomSheet && selectedStore != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                selectedStore = null
            },
            sheetState = sheetState,
            containerColor = colorResource(id = R.color.background),
            contentColor = colorResource(id = R.color.primary_text)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .background(colorResource(R.color.background))
            ) {
                selectedStore?.let { store ->
                    store.name?.let {
                        Text(
                            text = it,
                            style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 16.dp)

                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "${store.rating}",
                            color = colorResource(id = R.color.secondary_text)
                        )
                        StarRating(
                            rating = store.rating?.toFloat() ?: 0f,
                        )
                        Text(
                            text = "(${store.reviews?.size})",
                            color = colorResource(id = R.color.secondary_text)
                        )
                    }
                    val isOpen = isStoreOpen(place = store)
                    Text(
                        text = isOpen,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        Button(
                            onClick = {
                                val uri = Uri.parse("google.navigation:q=${store.address}")
                                val buttonIntent = Intent(Intent.ACTION_VIEW, uri)
                                buttonIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(buttonIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.bubble),
                                contentColor = colorResource(id = R.color.primary_text)
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "Directions",
                                style = TextStyle(fontSize = 17.sp)
                            )
                        }

                        if (store.websiteUri != null) {
                            Button(
                                onClick = {
                                    val uri = Uri.parse(store.websiteUri?.toString() ?: "")
                                    val buttonIntent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(buttonIntent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.bubble),
                                    contentColor = colorResource(id = R.color.primary_text)
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "Website",
                                    style = TextStyle(fontSize = 17.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}