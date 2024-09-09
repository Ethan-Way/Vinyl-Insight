package com.example.myapplication.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.data.DiscogsResponse
import com.example.myapplication.data.MasterDetailsResponse
import com.example.myapplication.data.RatingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast

class RecordSearch {

    private val api = DiscogsClient.discogsApi

    fun searchByBarcode(
        context: Context,
        barcode: String,
        callback: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit
    ) {
        api.searchByQuery(barcode).enqueue(object : Callback<DiscogsResponse> {
            override fun onResponse(
                call: Call<DiscogsResponse>,
                response: Response<DiscogsResponse>
            ) {
                if (response.isSuccessful) {
                    val releases = response.body()?.results
                    if (!releases.isNullOrEmpty()) {
                        val matchedRecord = releases.find { release ->
                            val releaseBarcodes = release.barcode?.map { it.replace(" ", "") }
                            releaseBarcodes?.contains(barcode) == true
                        }

                        if (matchedRecord != null) {
                            val record = matchedRecord.title
                            val year = matchedRecord.year
                            val country = matchedRecord.country
                            val format = matchedRecord.format
                            val label = matchedRecord.label!!.first()
                            val genre = matchedRecord.genre?.first()
                            val style = matchedRecord.style
                            val cover = releases.first().coverImageUrl
                            val masterUrl = releases.first().masterUrl

                            val cleanedRecord = record.replace(Regex("\\s*\\(.*\\)"), "")
                            val result = parseRecord(cleanedRecord)

                            val cleanedFormat = format?.first() + " (${
                                format?.drop(1)?.joinToString(separator = ", ")
                            })"
                            val cleanedStyle = style?.joinToString(separator = ", ")

                            if (masterUrl != null) {
                                fetchLowestPrice(context, masterUrl) { lowestPrice, numForSale, mainReleaseUrl ->
                                    if (mainReleaseUrl != null) {
                                        fetchRating(context, mainReleaseUrl) { averageRating, ratingCount ->
                                            if (result != null) {
                                                val (artist, album) = result
                                                SpotifyUtils.getSpotifyAccessToken { token ->
                                                    if (token != null) {
                                                        SpotifyUtils.searchSpotifyAlbum(
                                                            token,
                                                            album,
                                                            artist
                                                        ) { url ->
                                                            val spotifyJson = url?.first
                                                            val artistImage = url?.second
                                                            Handler(Looper.getMainLooper()).post {
                                                                callback(
                                                                    cleanedRecord,
                                                                    year,
                                                                    country,
                                                                    cleanedFormat,
                                                                    label,
                                                                    genre,
                                                                    cleanedStyle,
                                                                    cover,
                                                                    "$$lowestPrice",
                                                                    numForSale,
                                                                    spotifyJson,
                                                                    averageRating.toString(),
                                                                    ratingCount.toString(),
                                                                    artistImage
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "No releases found", Toast.LENGTH_SHORT).show()
                            callback(
                                "No releases found",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                            )
                        }
                    }
                } else {
                    Log.e("DiscogsRepository", "Response error: ${response.errorBody()?.string()}")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
                        callback(
                            "Error fetching data",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        )
                    }
                }
            }

            override fun onFailure(call: Call<DiscogsResponse>, t: Throwable) {
                Log.e("DiscogsRepository", "Network error: ${t.localizedMessage}")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Network error occurred", Toast.LENGTH_SHORT).show()
                    callback(
                        "Error fetching data",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                }
            }
        })
    }

    private fun fetchLowestPrice(
        context: Context,
        masterUrl: String,
        callback: (String?, String?, String?) -> Unit
    ) {
        api.getMasterDetails(masterUrl).enqueue(object : Callback<MasterDetailsResponse> {
            override fun onResponse(
                call: Call<MasterDetailsResponse>,
                response: Response<MasterDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    val lowestPrice = response.body()?.lowestPrice?.toString()
                    val numForSale = response.body()?.numForSale?.toString()
                    val mainReleaseUrl = response.body()?.mainReleaseUrl

                    Handler(Looper.getMainLooper()).post {
                        callback(lowestPrice, numForSale, mainReleaseUrl)
                    }
                } else {
                    Log.e("API Error", "Unsuccessful response: ${response.errorBody()?.string()}")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Failed to fetch lowest price", Toast.LENGTH_SHORT).show()
                        callback(null, null, null)
                    }
                }
            }

            override fun onFailure(call: Call<MasterDetailsResponse>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch data: ${t.message}")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Failed to fetch lowest price", Toast.LENGTH_SHORT).show()
                    callback(null, null, null)
                }
            }
        })
    }

    fun fetchRating(
        context: Context,
        mainReleaseUrl: String,
        callback: (Double?, Int?) -> Unit
    ) {
        val ratingUrl = "$mainReleaseUrl/rating"
        api.getRating(ratingUrl).enqueue(object : Callback<RatingResponse> {
            override fun onResponse(call: Call<RatingResponse>, response: Response<RatingResponse>) {
                if (response.isSuccessful) {
                    val averageRating = response.body()?.rating?.average
                    val ratingCount = response.body()?.rating?.count
                    Handler(Looper.getMainLooper()).post {
                        callback(averageRating, ratingCount)
                    }
                } else {
                    Log.e("API Error", "Unsuccessful response: ${response.errorBody()?.string()}")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Failed to fetch rating", Toast.LENGTH_SHORT).show()
                        callback(null, null)
                    }
                }
            }

            override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch data: ${t.message}")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Failed to fetch rating", Toast.LENGTH_SHORT).show()
                    callback(null, null)
                }
            }
        })
    }
}
