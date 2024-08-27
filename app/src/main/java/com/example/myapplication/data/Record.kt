package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val year: String,
    val country: String,
    val format: String,
    val label: String,
    val genre: String,
    val style: String,
    val cover: String,
    val lowestPrice: String,
    val numForSale: String,
    val spotifyLink: String
) {
    override fun toString(): String {
        return "$title ($year)"
    }
}
