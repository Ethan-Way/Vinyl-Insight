package com.example.myapplication

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecordSearch {

    private val api = DiscogsClient.discogsApi

    fun searchByBarcode(barcode: String, callback: (String, String?, String?, String?, String?, String?, String?, String?) -> Unit) {
        api.searchByQuery(barcode).enqueue(object : Callback<DiscogsResponse> {
            override fun onResponse(
                call: Call<DiscogsResponse>,
                response: Response<DiscogsResponse>
            ) {
                if (response.isSuccessful) {
                    val releases = response.body()?.results
                    if (!releases.isNullOrEmpty()) {
                        val record = releases.first().title
                        val year = releases.first().year
                        val country = releases.first().country
                        val format = releases.first().format
                        val label = releases.first().label!!.first()
                        val genre = releases.first().genre?.first()
                        val style = releases.first().style

                        val cover = releases.first().coverImageUrl

                        val cleanedRecord = record.replace(Regex("\\s*\\(.*\\)"), "")
                        val cleanedFormat = format?.first() + " (${format?.drop(1)?.joinToString(separator = ", ")})"
                        val cleanedStyle = style?.joinToString(separator = ", ")

                        callback(cleanedRecord, year, country, cleanedFormat, label, genre, cleanedStyle, cover)
                    } else {
                        callback("No releases found", "", "", "", "", "", "", "")
                    }
                } else {
                    Log.e(
                        "DiscogsRepository",
                        "Response error: ${response.errorBody()?.string()}"
                    )
                    callback("Error fetching data", "", "", "", "", "", "", "")
                }
            }

            override fun onFailure(call: Call<DiscogsResponse>, t: Throwable) {
                Log.e("DiscogsRepository", "Network error: ${t.localizedMessage}")
                callback("Error fetching data", "", "", "", "", "", "", "")
            }
        })
    }
}
