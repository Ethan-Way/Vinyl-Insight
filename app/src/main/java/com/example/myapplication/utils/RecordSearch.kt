package com.example.myapplication.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.DiscogsResponse
import com.example.myapplication.MasterDetailsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecordSearch {

    private val api = DiscogsClient.discogsApi

    fun searchByBarcode(
        barcode: String,
        callback: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit
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
                            release.barcode?.contains(barcode) == true
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
                                fetchLowestPrice(masterUrl) { lowestPrice, numForSale ->
                                    if (result != null) {
                                        val (artist, album) = result
                                        SpotifyUtils.getSpotifyAccessToken { token ->
                                            if (token != null) {
                                                SpotifyUtils.searchSpotifyAlbum(
                                                    token,
                                                    album,
                                                    artist
                                                ) { url ->
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
                                                            url
                                                        )
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
                                null
                            )
                        }
                    }
                } else {
                    Log.e(
                        "DiscogsRepository",
                        "Response error: ${response.errorBody()?.string()}"
                    )
                    Handler(Looper.getMainLooper()).post {
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
                            null
                        )
                    }
                }
            }

            override fun onFailure(call: Call<DiscogsResponse>, t: Throwable) {
                Log.e("DiscogsRepository", "Network error: ${t.localizedMessage}")
                Handler(Looper.getMainLooper()).post {
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
                        null
                    )
                }
            }
        })
    }

    private fun fetchLowestPrice(masterUrl: String, callback: (String?, String?) -> Unit) {
        api.getMasterDetails(masterUrl).enqueue(object : Callback<MasterDetailsResponse> {
            override fun onResponse(
                call: Call<MasterDetailsResponse>,
                response: Response<MasterDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    val lowestPrice = response.body()?.lowestPrice?.toString()
                    val numForSale = response.body()?.numForSale?.toString()

                    Handler(Looper.getMainLooper()).post {
                        callback(lowestPrice, numForSale)
                    }
                } else {
                    Log.e("API Error", "Unsuccessful response: ${response.errorBody()?.string()}")
                    Handler(Looper.getMainLooper()).post {
                        callback(null, null)
                    }
                }
            }

            override fun onFailure(call: Call<MasterDetailsResponse>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch data: ${t.message}")
                // Call the callback with error on the main thread
                Handler(Looper.getMainLooper()).post {
                    callback(null, null)
                }
            }
        })
    }
}