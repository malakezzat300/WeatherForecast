package com.malakezzat.weatherforecast.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.malakezzat.weatherforecast.model.Alert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM ALERTS")
    fun getAllAlerts() : Flow<List<Alert>>

    @Insert
    fun insertAllAlerts(vararg alert: Alert)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAlert(alert: Alert)

    @Query("DELETE FROM ALERTS WHERE deleteId = :alertId")
    fun deleteAlertById(alertId: String)

    @Delete
    fun deleteAlert(alert: Alert)
}