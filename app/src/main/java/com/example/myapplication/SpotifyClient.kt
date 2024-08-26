package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SpotifyClient {
    private const val BASE_URL = "https://api.spotify.com/"

    val spotifyApi: SpotifyApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(SpotifyApi::class.java)
    }
}