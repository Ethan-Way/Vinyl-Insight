package com.example.myapplication.utils

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