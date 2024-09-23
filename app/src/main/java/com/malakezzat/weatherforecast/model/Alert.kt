package com.malakezzat.weatherforecast.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromTime: String,
    val toTime: String,
    val fromDate: String,
    val toDate: String,
    val type: Type
)

enum class Type {
    ALERT,
    NOTIFICATION
}

