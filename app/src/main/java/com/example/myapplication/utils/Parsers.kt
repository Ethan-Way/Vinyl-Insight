package com.example.myapplication.utils

import org.json.JSONObject

fun parseRecord(record: String): Pair<String, String>? {
    val parts = record.split(" - ")
    return if (parts.size == 2) {
        val artist = parts[0].trim()
        val album = parts[1].trim()
        Pair(artist, album)
    } else {
        null
    }
}

fun extractLinks(json: String): Pair<String, String>? {
    val jsonObject = JSONObject(json)
    val albums = jsonObject.getJSONObject("albums")
    val items = albums.getJSONArray("items")

    if (items.length() > 0) {
        val albumItem = items.getJSONObject(0)

        // Extract the artist link
        val artistArray = albumItem.getJSONArray("artists")
        val artistObject = artistArray.getJSONObject(0)
        val artistLink = artistObject.getJSONObject("external_urls").getString("spotify")

        // Extract the album link
        val albumLink = albumItem.getJSONObject("external_urls").getString("spotify")

        return Pair(artistLink, albumLink)
    }

    return null
}