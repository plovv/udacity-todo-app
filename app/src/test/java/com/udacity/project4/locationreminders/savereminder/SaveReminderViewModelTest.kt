package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        dataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // setSelectedLocation
    @Test
    fun onSetSelectedLocation_POI_locationAddedAndNavigationDestinationSet() {
        val lat = 1.1
        val long = 2.2
        val poi = PointOfInterest(LatLng(lat, long), "test id", "test")

        saveReminderViewModel.setSelectedLocation(poi, lat, long)

        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue()?.placeId, `is`(poi.placeId))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue()?.name, `is`(poi.name))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue()?.latLng?.latitude, `is`(lat))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue()?.latLng?.longitude, `is`(long))

        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(lat))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(long))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(poi.name))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.BackTo(R.id.saveReminderFragment)))
    }

    @Test
    fun onSetSelectedLocation_noPOI_locationAddedAndNavigationDestinationSet() {
        val lat = 1.1
        val long = 2.2

        saveReminderViewModel.setSelectedLocation(null, lat, long)

        assertThat(saveReminderViewModel.selectedPOI.value, `is`(nullValue()))

        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(lat))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(long))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`("Pin location"))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.BackTo(R.id.saveReminderFragment)))
    }

    // hasSelectedLocation
    @Test
    fun onSetSelectedLocation_hasSelectedLocation_isTrue() {
        val lat = 1.1
        val long = 2.2
        val poi = PointOfInterest(LatLng(lat, long), "test id", "test")

        saveReminderViewModel.setSelectedLocation(poi, lat, long)

        assertThat(saveReminderViewModel.hasSelectedLocation(), `is`(true))
    }

    @Test
    fun onNonSelectedLocation_hasSelectedLocation_isFalse() {
       assertThat(saveReminderViewModel.hasSelectedLocation(), `is`(false))
    }

    // validateEnteredData
    @Test
    fun validation_nonValidData_showError() {
        val desc = "Test description"
        val long = 1.1
        val lat = 2.2
        val id = "Test id"

        val reminder = ReminderDataItem(
            null,
            desc,
            null,
            lat,
            long,
            id
        )

        val validation = saveReminderViewModel.validateEnteredData(reminder)

        assertThat(validation, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(notNullValue()))
    }

    @Test
    fun validation_validData_succeeds() {
        val title = "Test title"
        val desc = "Test description"
        val location = "Test location"
        val long = 1.1
        val lat = 2.2
        val id = "Test id"

        val reminder = ReminderDataItem(
            title,
            desc,
            location,
            lat,
            long,
            id
        )

        assertThat(saveReminderViewModel.validateEnteredData(reminder), `is`(true))
    }

    // saveReminder
    @Test
    fun checkSaveReminder() = runBlockingTest {
        val title = "Test title"
        val desc = "Test description"
        val location = "Test location"
        val long = 1.1
        val lat = 2.2
        val id = "Test id"

        val reminder = ReminderDataItem(
            title,
            desc,
            location,
            lat,
            long,
            id
        )

        saveReminderViewModel.saveReminder(reminder)

        val result = dataSource.getReminder(id)
        var savedReminder: ReminderDataItem? = null

        if (result is Result.Success<*>) {
                val r = result.data as ReminderDTO

                savedReminder = ReminderDataItem (
                    r.title,
                    r.description,
                    r.location,
                    r.latitude,
                    r.longitude,
                    r.id
                )
        }

        assertThat(savedReminder, `is`(notNullValue()))

        assertThat(savedReminder?.id, `is`(id))
        assertThat(savedReminder?.title , `is`(title))
        assertThat(savedReminder?.description, `is`(desc))
        assertThat(savedReminder?.latitude, `is`(lat))
        assertThat(savedReminder?.longitude, `is`(long))
        assertThat(savedReminder?.location, `is`(location))
    }

    @Test
    fun saveReminder_showLoading() {
        val title = "Test title"
        val desc = "Test description"
        val location = "Test location"
        val long = 1.1
        val lat = 2.2
        val id = "Test id"

        val reminder = ReminderDataItem(
            title,
            desc,
            location,
            lat,
            long,
            id
        )

        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.saveReminder(reminder)

        // Loading indicator is shown.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()

        // Loading indicator is hidden.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

}