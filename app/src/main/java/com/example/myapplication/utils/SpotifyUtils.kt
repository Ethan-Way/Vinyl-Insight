package com.example.myapplication.utils

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object SpotifyUtils {
    private const val CLIENT_ID = ""
    private const val CLIENT_SECRET = ""
    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"
    private const val SEARCH_URL = "https://api.spotify.com/v1/search"

    private fun getBase64Credentials(): String {
        val credentials = "$CLIENT_ID:$CLIENT_SECRET"
        return android.util.Base64.encodeToString(credentials.toByteArray(), android.util.Base64.NO_WRAP)
    }

    fun getSpotifyAccessToken(callback: (String?) -> Unit) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(requestBody)
            .addHeader("Authorization", "Basic ${getBase64Credentials()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val jsonObject = JSONObject(responseData)
                    val accessToken = jsonObject.getString("access_token")
                    callback(accessToken)
                } else {
                    callback(null)
                }
            }
        })
    }

    fun searchSpotifyAlbum(token: String, albumName: String, artistName: String, callback: (String?) -> Unit) {
        val client = OkHttpClient()
        val url = "$SEARCH_URL?q=album:${albumName.replace(" ", "+")}+artist:${artistName.replace(" ", "+")}&type=album"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    callback(responseData)
                } else {
                    callback(null)
                }
            }
        })
    }
}
