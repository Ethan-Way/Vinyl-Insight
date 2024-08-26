package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyApi {
    @GET("v1/search")
    fun searchAlbum(
        @Header("Authorization") authHeader: String,
        @Query("q") query: String,
        @Query("type") type: String = "album"
    ): Call<SpotifySearchResponse>
}
