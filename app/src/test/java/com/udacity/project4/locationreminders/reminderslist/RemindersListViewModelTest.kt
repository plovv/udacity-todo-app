package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // rule to test live data
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun reminder_getReminder() = runBlockingTest {
        val title = "Test title"
        val desc = "Test description"
        val location = "Test location"
        val long = 1.1
        val lat = 2.2
        val id = "Test id"

        dataSource.saveReminder(ReminderDTO(
            title,
            desc,
            location,
            lat,
            long,
            id
        ))

        remindersListViewModel.loadReminders()

        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()

        assertThat(reminders[0].title, `is`(title))
        assertThat(reminders[0].description, `is`(desc))
        assertThat(reminders[0].location, `is`(location))
        assertThat(reminders[0].longitude, `is`(long))
        assertThat(reminders[0].latitude, `is`(lat))
        assertThat(reminders[0].id, `is`(id))
    }

    @Test
    fun reminders_check_loading() {
        mainCoroutineRule.pauseDispatcher()

        // Load the reminders in the view model.
        remindersListViewModel.loadReminders()

        // Loading indicator is shown.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()

        // Loading indicator is hidden.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun whenRemindersFail_callShowErrorMessage() {
        // Set to return error.
        dataSource.setReturnError(true)
        remindersListViewModel.loadReminders()

        // Error message is not null (which triggers an error message to be shown).
        assertThat(remindersListViewModel.showErrorMessage.getOrAwaitValue(), notNullValue())
    }

    @Test
    fun whenRemindersEmpty_callShowNoData() {
        // Reminders are empty
        remindersListViewModel.loadReminders()

        // Show no data is true.
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), Matchers.`is`(true))
    }

}