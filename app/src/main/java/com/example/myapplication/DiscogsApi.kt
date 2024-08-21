package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DiscogsApi {
    @GET("database/search")
    fun searchByQuery(
        @Query("q") query: String,
    ): Call<DiscogsResponse>
}
