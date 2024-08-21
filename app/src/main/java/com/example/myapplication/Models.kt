package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class DiscogsResponse(
    val pagination: Pagination,
    val results: List<Release>
)

data class Pagination(
    val page: Int,
    val pages: Int,
    @SerializedName("per_page") val perPage: Int,
    val items: Int,
    @SerializedName("urls") val urls: Map<String, String>
)

data class Release(
    val title: String,
    @SerializedName("country") val country: String?,
    val year: String?,
    val format: List<String>?,
    val label: List<String>?,
    val type: String?,
    val genre: List<String>?,
    val style: List<String>?,
    val id: Int,
    @SerializedName("barcode") val barcode: List<String>?,
    @SerializedName("user_data") val userData: UserData?,
    @SerializedName("master_id") val masterId: Int,
    @SerializedName("master_url") val masterUrl: String?,
    val uri: String?,
    @SerializedName("catno") val catalogNumber: String?,
    @SerializedName("thumb") val thumbUrl: String?,
    @SerializedName("cover_image") val coverImageUrl: String?,
    @SerializedName("resource_url") val resourceUrl: String?,
    val community: Community?,
    @SerializedName("format_quantity") val formatQuantity: Int?,
    val formats: List<Format>?
)

data class UserData(
    @SerializedName("in_wantlist") val inWantlist: Boolean,
    @SerializedName("in_collection") val inCollection: Boolean
)

data class Community(
    val want: Int?,
    val have: Int?
)

data class Format(
    val name: String?,
    val qty: String?,
    val text: String?,
    val descriptions: List<String>?
)
