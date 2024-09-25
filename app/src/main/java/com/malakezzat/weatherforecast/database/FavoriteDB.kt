package com.malakezzat.weatherforecast.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_list")
data class FavoriteDB (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val lon: Double,
    val lat: Double,
    val address : String,
    var deleteId : String,
)