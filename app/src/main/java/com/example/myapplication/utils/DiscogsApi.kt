package com.example.myapplication.utils

import com.example.myapplication.data.DiscogsResponse
import com.example.myapplication.data.MasterDetailsResponse
import com.example.myapplication.data.RatingResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface DiscogsApi {
    @GET("database/search")
    fun searchByQuery(
        @Query("q") query: String,
    ): Call<DiscogsResponse>

    @GET
    fun getMasterDetails(@Url url: String): Call<MasterDetailsResponse>

    @GET
    fun getRating(@Url url: String): Call<RatingResponse>
}
