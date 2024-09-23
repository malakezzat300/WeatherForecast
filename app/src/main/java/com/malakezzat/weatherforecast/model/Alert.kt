package com.malakezzat.weatherforecast.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromTime: Long,
    val toTime: Long,
    val fromDate: Long,
    val toDate: Long,
    val type: Int
)

