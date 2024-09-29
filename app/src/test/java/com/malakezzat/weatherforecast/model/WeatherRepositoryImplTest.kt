package com.malakezzat.weatherforecast.model

import com.malakezzat.weatherforecast.database.FakeLocalDataSource
import com.malakezzat.weatherforecast.database.FavoriteDB
import com.malakezzat.weatherforecast.database.IWeatherLocalDataSource
import com.malakezzat.weatherforecast.network.FakeRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.jupiter.api.Assertions.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var fakeLocalDataSource : FakeLocalDataSource
    private lateinit var fakeRemoteDataSource : FakeRemoteDataSource
    private lateinit var repository : WeatherRepository

    val favorite1 = FavoriteDB(1,
        1.23456789,
        1.23456789,
        "message1",
        "12ab")

    val favorite2 = FavoriteDB(2,
        2.23456789,
        2.23456789,
        "message2",
        "22ab")

    val favorite3 = FavoriteDB(3,
        3.23456789,
        3.23456789,
        "message3",
        "32ab")



    @Before
    fun setup(){
        fakeLocalDataSource = FakeLocalDataSource(mutableListOf())
        fakeRemoteDataSource = FakeRemoteDataSource()
        repository = WeatherRepositoryImpl(fakeRemoteDataSource,fakeLocalDataSource)
    }

    @Test
    fun insertFavoriteTest_noInitialFavorites_addsFirstFavorite() = runTest {
        repository.insertFavorite(favorite1)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1)))
    }

    @Test
    fun insertFavoriteTest_addMultipleFavorites_insertsSuccessfully() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite2)
        repository.insertFavorite(favorite3)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1, favorite2, favorite3)))
    }

    @Test
    fun insertFavoriteTest_addDuplicateFavorite_doesNotAddDuplicate() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite1) // Duplicate insertion

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1))) // Only one instance of favorite1
    }

    @Test
    fun insertFavoriteTest_addMixOfUniqueAndDuplicateFavorites_handlesCorrectly() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite2)
        repository.insertFavorite(favorite1) // Duplicate insertion

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1, favorite2)))
    }

    @Test
    fun insertFavoriteTest_afterDeletion_addNewFavoriteSuccessfully() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite2)

        repository.deleteFavorite(favorite2)

        repository.insertFavorite(favorite3)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1, favorite3)))
    }

    @Test
    fun insertFavoriteTest_emptyList_addFavoriteSuccessfully() = runTest {
        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(emptyList()))

        repository.insertFavorite(favorite1)

        val updatedList = repository.getFavoriteData().first()

        assertThat(updatedList, `is`(listOf(favorite1)))
    }

    @Test
    fun deleteFavoriteTest_existingFavorite_removesFavoriteSuccessfully() = runTest {
        repository.insertFavorite(favorite1)

        repository.deleteFavorite(favorite1)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(emptyList()))
    }

    @Test
    fun deleteFavoriteTest_nonExistentFavorite_doesNotAlterList() = runTest {
        repository.insertFavorite(favorite1)

        repository.deleteFavorite(favorite2)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1)))
    }

    @Test
    fun deleteFavoriteTest_emptyList_doesNothing() = runTest {
        repository.deleteFavorite(favorite1)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(emptyList()))
    }

    @Test
    fun deleteFavoriteTest_multipleFavorites_deletesCorrectFavorite() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite2)

        repository.deleteFavorite(favorite1)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite2)))
    }

    @Test
    fun deleteFavoriteTest_multipleFavorites_deletesAllFavorites() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite2)

        repository.deleteFavorite(favorite1)
        repository.deleteFavorite(favorite2)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(emptyList()))
    }

    @Test
    fun deleteFavoriteTest_duplicates_returnZeroSize() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite1)

        repository.deleteFavorite(favorite1)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList.size, `is`(0))
    }

    @Test
    fun getFavoriteDataTest_noFavorites_returnEmptyList() = runTest {
        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(emptyList()))
    }

    @Test
    fun getFavoriteDataTest_insertOneFav_returnOneFav() = runTest {
        repository.insertFavorite(favorite1)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1)))
    }

    @Test
    fun getFavoriteDataTest_insertMultipleFav_returnAllFav() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite2)
        repository.insertFavorite(favorite3)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1, favorite2, favorite3)))
    }

    @Test
    fun getFavoriteDataTest_insertDuplicateFav_returnUniqueFav() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite1)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1)))
    }

    @Test
    fun getFavoriteDataTest_deleteFav_returnRemainingFav() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite2)
        repository.insertFavorite(favorite3)

        repository.deleteFavorite(favorite2)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1, favorite3)))
    }

    @Test
    fun getFavoriteDataTest_multipleInserts_noDuplicates() = runTest {
        repository.insertFavorite(favorite1)
        repository.insertFavorite(favorite2)
        repository.insertFavorite(favorite1)

        val favoriteList = repository.getFavoriteData().first()

        assertThat(favoriteList, `is`(listOf(favorite1, favorite2)))
    }



}