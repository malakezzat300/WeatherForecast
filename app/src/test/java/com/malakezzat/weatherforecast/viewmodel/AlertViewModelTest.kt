package com.malakezzat.weatherforecast.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.malakezzat.weatherforecast.alert.viewmodel.AlertViewModel
import com.malakezzat.weatherforecast.database.FakeLocalDataSource
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.FakeWeatherRepository
import com.malakezzat.weatherforecast.network.FakeRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertViewModelTest {

    lateinit var alertViewModel: AlertViewModel
    private lateinit var fakeLocalDataSource : FakeLocalDataSource
    private lateinit var fakeRemoteDataSource : FakeRemoteDataSource
    private lateinit var repository : FakeWeatherRepository

    @Before
    fun setup() {
        fakeLocalDataSource = FakeLocalDataSource(mutableListOf())
        fakeRemoteDataSource = FakeRemoteDataSource()
        repository = FakeWeatherRepository(fakeRemoteDataSource,fakeLocalDataSource)
        alertViewModel = AlertViewModel(repository)
    }

    private val alert1 = Alert(1,123456,123456,
        123456,123456,1,"message1","123-abc1","12ab1")
    private val alert2 = Alert(2,123456,123456,
        123456,123456,2,"message2","123-abc2","12ab2")
    private val alert3 = Alert(3,123456,123456,
        123456,123456,3,"message3","123-abc3","12ab3")


    @Test
    fun addAlertTest_addAlert_addsFirstAlert() = runTest {
        alertViewModel.addAlert(alert1)

        val apiState = alertViewModel.alertList.first()

        assertThat(apiState, `is`(ApiState.Success(listOf(alert1))))
    }

    @Test
    fun addAlertTest_addDuplicateAlert_doesNotAddDuplicate() = runTest {
        alertViewModel.fetchAlertData()

        alertViewModel.addAlert(alert1)
        alertViewModel.addAlert(alert1)

        val alertList = alertViewModel.alertList.value

        assertThat(alertList, `is`(ApiState.Success(listOf(alert1))))
    }

    @Test
    fun addAlertTest_addMixOfUniqueAndDuplicateAlerts_handlesCorrectly() = runTest {
        alertViewModel.addAlert(alert1)
        alertViewModel.addAlert(alert2)
        alertViewModel.addAlert(alert1)

        val alertList = alertViewModel.alertList.value

        assertThat(alertList, `is`(ApiState.Success(listOf(alert1, alert2))))
    }

    @Test
    fun addAlertTest_afterDeletion_addNewAlertSuccessfully() = runTest {
        alertViewModel.addAlert(alert1)
        alertViewModel.addAlert(alert2)

        alertViewModel.removeAlert(alert2)

        alertViewModel.addAlert(alert3)

        val alertList = alertViewModel.alertList.first()

        assertThat(alertList, `is`(ApiState.Success(listOf(alert1, alert3))))
    }

    @Test
    fun addAlertTest_isEmpty() = runTest {
        val updatedList = (alertViewModel.alertList.value as ApiState.Success).data

        assertThat(updatedList, `is`(emptyList()))
    }

    @Test
    fun removeAlertTest_existingAlert_removesAlertSuccessfully() = runTest {
        alertViewModel.addAlert(alert1)

        alertViewModel.removeAlert(alert1)

        val alertList = alertViewModel.alertList.first()

        assertThat(alertList, `is`(ApiState.Success(emptyList())))
    }

    @Test
    fun removeAlertTest_nonExistentAlert_doesNotAlterList() = runTest {
        alertViewModel.addAlert(alert1)

        alertViewModel.removeAlert(alert2)

        val alertList = alertViewModel.alertList.value

        assertThat(alertList, `is`(ApiState.Success(listOf(alert1))))
    }

    @Test
    fun removeAlertTest_emptyList_doesNothing() = runTest {
        alertViewModel.removeAlert(alert1)

        val alertList = alertViewModel.alertList.value

        assertThat(alertList, `is`(ApiState.Success(emptyList())))
    }

    @Test
    fun removeAlertTest_multipleAlerts_deletesCorrectAlert() = runTest {
        alertViewModel.addAlert(alert1)
        alertViewModel.addAlert(alert2)

        alertViewModel.removeAlert(alert1)

        val alertList = alertViewModel.alertList.value

        assertThat(alertList, `is`(ApiState.Success(listOf(alert2))))
    }

    @Test
    fun removeAlertTest_multipleAlerts_deletesAllAlerts() = runTest {
        alertViewModel.addAlert(alert1)
        alertViewModel.addAlert(alert2)

        alertViewModel.removeAlert(alert1)
        alertViewModel.removeAlert(alert2)

        val alertList = alertViewModel.alertList.value

        assertThat(alertList, `is`(ApiState.Success(emptyList())))
    }

    @Test
    fun removeAlertTest_duplicates_returnZero() = runTest {
        alertViewModel.addAlert(alert1)
        alertViewModel.addAlert(alert1)

        alertViewModel.removeAlert(alert1)

        val alertList = alertViewModel.alertList.first()

        assertThat(alertList, `is`(ApiState.Success(emptyList())))
    }


}