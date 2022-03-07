package com.udacity.project4.locationreminders.reminderslist

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {

    private lateinit var dataSource: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin

        appContext = getApplicationContext()
        dataSource = FakeAndroidDataSource()

        val myModule = module {
            viewModel { RemindersListViewModel(appContext, dataSource) }
        }

        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
    }

    @Test
    fun clickAddReminder_navigateToSaveReminderFragment() {
        // GIVEN - reminders screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - On add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the add screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun onLoad_withReminders_listDisplayed() {
        // GIVEN - existing reminder
        val title = "Test title"
        val desc = "Test description"
        val location = "Test location"
        val long = 1.1
        val lat = 2.2
        val id = "Test id"

        runBlockingTest {
            dataSource.saveReminder(
                ReminderDTO(
                    title,
                    desc,
                    location,
                    lat,
                    long,
                    id
                )
            )
        }

        // WHEN - On fragment launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - Reminder is displayed
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))

        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(desc)).check(matches(isDisplayed()))
        onView(withText(location)).check(matches(isDisplayed()))
    }

    @Test
    fun onLoad_noReminders_noDataDisplayed() {
        // GIVEN - no reminder data
        // WHEN - On fragment launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - no data is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    @TargetApi(29)
    fun onLoad_withError_errorMsgDisplayed() {
        // GIVEN - An error
        val fakeDataSource = (dataSource as FakeAndroidDataSource)
        fakeDataSource.setReturnError(true)

        // WHEN - On fragment launched
        val fragScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - Reminder is displayed
        var activity: Activity? = null

        fragScenario.withFragment {
            activity = this.requireActivity()
        }

        // this seems to be an issue on SDK = 31 but works with lower level
        onView(withText(fakeDataSource.returnErrorMessage)).inRoot(withDecorView(not(`is`(activity?.window?.decorView))))
            .check(matches(isDisplayed()))
    }

}