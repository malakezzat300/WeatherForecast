package com.malakezzat.weatherforecast.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM FAVORITE_LIST")
    fun getFavoriteData() : Flow<List<FavoriteDB>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favoriteDB: FavoriteDB)

    @Query("DELETE FROM FAVORITE_LIST WHERE deleteId = :favoriteId")
    suspend fun deleteFavoriteById(favoriteId: String)

    @Delete
    suspend fun deleteFavorite(favoriteDB :FavoriteDB)

}