package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.icu.util.LocaleData
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.PlacesStatusCodes

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
    val placeFields = listOf(
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS,
        Place.Field.RATING,
        Place.Field.OPENING_HOURS,
        Place.Field.REVIEWS,
        Place.Field.WEBSITE_URI,
        Place.Field.ID
    )

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

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun isStoreOpen(place: Place): AnnotatedString {
    val openingHours = place.openingHours?.periods ?: return AnnotatedString("Hours not available")

    val now = LocalDateTime.now()
    val currentDayOfWeek = now.dayOfWeek
    val currentTime = now.toLocalTime()

    val periodToday = openingHours.find { period ->
        period.open.day.toString() == currentDayOfWeek.toString()
    }

    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    if (periodToday != null) {
        val openTime = LocalTime.of(periodToday.open.time.hours, periodToday.open.time.minutes)
        val closeTime = periodToday.close?.let {
            LocalTime.of(it.time.hours, it.time.minutes)
        }

        val isOpen = if (closeTime != null && closeTime.isBefore(openTime)) {
            currentTime.isAfter(openTime) || currentTime.isBefore(closeTime)
        } else {
            currentTime.isAfter(openTime) && (closeTime == null || currentTime.isBefore(closeTime))
        }

        val openText = "Open"
        val closingText = closeTime?.let { " ⋅ Closes ${it.format(timeFormatter)}" } ?: ""

        return if (isOpen) {
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = colorResource(id = R.color.open_text))) {
                    append(openText)
                }
                withStyle(style = SpanStyle(color = colorResource(id = R.color.secondary_text))) {
                    append(closingText)
                }
            }
        } else {
            AnnotatedString("Closed")
        }
    } else {
        return AnnotatedString("")
    }
}
fun getStoreImages(
    placesClient: PlacesClient,
    placeId: String,
    onImagesReceived: (List<String>) -> Unit
) {
    val fields = listOf(Place.Field.PHOTO_METADATAS)
    val placeRequest = FetchPlaceRequest.builder(placeId, fields).build()

    placesClient.fetchPlace(placeRequest).addOnSuccessListener { response ->
        val place = response.place
        val photoMetadatas = place.photoMetadatas

        if (photoMetadatas.isNullOrEmpty()) {
            Log.w("TAG", "No photo metadata.")
            onImagesReceived(emptyList())
            return@addOnSuccessListener
        }

        val photoUrls = mutableListOf<String>()

        photoMetadatas.forEach { photoMetadata ->
            val photoRequest = FetchResolvedPhotoUriRequest.builder(photoMetadata)
                .setMaxWidth(500)  // Adjust width as needed
                .setMaxHeight(300) // Adjust height as needed
                .build()

            placesClient.fetchResolvedPhotoUri(photoRequest).addOnSuccessListener { photoUriResponse ->
                val photoUri = photoUriResponse.uri.toString()
                photoUrls.add(photoUri)

                if (photoUrls.size == photoMetadatas.size) {
                    onImagesReceived(photoUrls)
                }
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e("TAG", "Photo fetch failed: ${exception.message}")
                }
            }
        }
    }.addOnFailureListener { exception ->
        if (exception is ApiException) {
            Log.e("TAG", "Place not found: ${exception.message}")
        }
        onImagesReceived(emptyList())
    }
}
