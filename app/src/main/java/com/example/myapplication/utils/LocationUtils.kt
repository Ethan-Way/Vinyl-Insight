package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest

@SuppressLint("MissingPermission")
fun getUserLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        onLocationReceived(location)
    }
}

@SuppressLint("MissingPermission")
fun searchNearbyStores(
    placesClient: PlacesClient,
    location: LatLng,
    onPlacesFetched: (List<Place>) -> Unit
) {
    val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

    val result = calculateRectangle(20, location)

    val rectangle = RectangularBounds.newInstance(result.first, result.second)

    val request = SearchByTextRequest.builder("Vinyl record store near me", placeFields)
        .setLocationRestriction(rectangle)
        .build()

    placesClient.searchByText(request)
        .addOnSuccessListener { response ->
            val places = response.places
            Log.d("placesResponse", places.toString())
            onPlacesFetched(places)
        }
        .addOnFailureListener { exception ->
            Log.e("placesResponse", "Failed to find places: ${exception.message}")
        }
}