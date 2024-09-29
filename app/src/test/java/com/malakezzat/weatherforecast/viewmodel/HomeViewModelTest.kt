package com.malakezzat.weatherforecast.viewmodel

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.malakezzat.weatherforecast.database.FakeLocalDataSource
import com.malakezzat.weatherforecast.home.viewmodel.HomeViewModel
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.model.City
import com.malakezzat.weatherforecast.model.Clouds
import com.malakezzat.weatherforecast.model.Coord
import com.malakezzat.weatherforecast.model.FakeWeatherRepository
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.IWeatherRepository
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.Main
import com.malakezzat.weatherforecast.model.Sys
import com.malakezzat.weatherforecast.model.Weather
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.model.WeatherResponse
import com.malakezzat.weatherforecast.model.Wind
import com.malakezzat.weatherforecast.network.FakeRemoteDataSource
import getOrAwaitValue
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.mockito.kotlin.notNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    lateinit var homeViewModel: HomeViewModel
    private lateinit var fakeLocalDataSource : FakeLocalDataSource
    private lateinit var fakeRemoteDataSource : FakeRemoteDataSource
    private lateinit var repository : FakeWeatherRepository

    val weatherResponse = WeatherResponse(
        1,
        Coord(1.23, 1.23),
        listOf(
            Weather(
                802,
                "Clouds",
                "clear sky",
                "03n"
            )
        ),
        "base",
        Main(
            298.05,
            298.74,
            298.05,
            298.05,
            1012,
            82,
            1012,
            1012,
            "123"
        ),
        100,
        Wind(5.56, 277, 5.1),
        Clouds(33),
        123456789,
        Sys(123456, 456123, "eg", 123456, 123456),
        0,
        "0",
        200
    )



    val forecastResponse =
        ForecastResponse(2,
            "200",
            210,
            40,
            listOf(
                ListF(123456,
                Main(298.05,
                    298.74,
                    298.05,
                    298.05,
                    1012,
                    82,
                    1012,
                    1012,
                    "123"),
                listOf(
                    Weather( 802,
                        "Clouds",
                        "clear sky",
                        "03n")
                ),
                Clouds(15),
                Wind(5.56,277,5.1),
                123456,
                5.5,
                Sys(123456,456123,"eg",123456,123456),
                "45678912")
            ),
            City(1,"london",
                Coord(1.2,1.2),
                "UK",
                123456,
                0,
                45564545,
                64),
            4554
        )


    @Before
    fun setup() {
        fakeLocalDataSource = FakeLocalDataSource(mutableListOf())
        fakeRemoteDataSource = FakeRemoteDataSource()
        repository = FakeWeatherRepository(fakeRemoteDataSource,fakeLocalDataSource)
        homeViewModel = HomeViewModel(repository)
    }

    @Test
    fun fetchWeatherDataTest_success_fetchesWeatherData() = runTest {
        homeViewModel.fetchWeatherData(1.234, 2.345)

        val apiState = homeViewModel.currentWeather.asLiveData().getOrAwaitValue()

        when (apiState) {
            is ApiState.Success -> {
                assertThat(weatherResponse, `is`(apiState.data))
            }
            else -> fail("Expected Success state but got $apiState")
        }
    }

    @Test
    fun fetchWeatherDataTest_error_loggingException() = runTest {
        homeViewModel.fetchWeatherData(1.234, 2.345)

        val apiState = ApiState.Failure(Throwable("network error"))

        assertTrue(apiState is ApiState.Failure)
    }

    @Test
    fun fetchWeatherDataTest_repositoryCalledWithCorrectParams() = runTest {
        val lat = 1.234
        val lon = 2.345

        homeViewModel.fetchWeatherData(lat, lon)

        assertThat(repository.getWeatherOverNetwork(lat, lon, "", ""),`is`(ApiState.Success(weatherResponse)))
    }

    @Test
    fun fetchWeatherDataTest_liveDataUpdatesOnSuccess() = runTest {
        homeViewModel.fetchWeatherData(1.234, 2.345)

        val apiState = homeViewModel.currentWeather.asLiveData().getOrAwaitValue()

        assertEquals(weatherResponse, (apiState as ApiState.Success).data)
    }

    @Test
    fun fetchForecastDataTest_success_fetchesForecastData() = runTest {
        homeViewModel.fetchForecastData(1.234, 2.345, "metric", "en")

        val apiState = homeViewModel.currentForecast.asLiveData().getOrAwaitValue()

        when (apiState) {
            is ApiState.Success -> {
                assertThat(forecastResponse, `is`(apiState.data))
            }
            else -> fail("Expected Success state but got $apiState")
        }
    }

    @Test
    fun fetchForecastDataTest_error_loggingException() = runTest {
        homeViewModel.fetchForecastData(1.234, 2.345, "metric", "en")

        val apiState = ApiState.Failure(Throwable("network error"))

        assertTrue(apiState is ApiState.Failure)
    }

    @Test
    fun fetchForecastDataTest_repositoryCalledWithCorrectParams() = runTest {
        val lat = 1.234
        val lon = 2.345
        val units = "metric"
        val lang = "en"

        homeViewModel.fetchForecastData(lat, lon, units, lang)

        val apiState = homeViewModel.currentForecast.asLiveData().getOrAwaitValue()

        assertEquals(forecastResponse, (apiState as ApiState.Success).data)
    }

    @Test
    fun fetchForecastDataTest_liveDataUpdatesOnSuccess() = runTest {
        homeViewModel.fetchForecastData(1.234, 2.345, "metric", "en")

        val apiState = homeViewModel.currentForecast.asLiveData().getOrAwaitValue()

        assertEquals(forecastResponse, (apiState as ApiState.Success).data)
    }

}