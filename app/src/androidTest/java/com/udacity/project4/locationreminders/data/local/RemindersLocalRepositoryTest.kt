package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var remindersDao: RemindersDao
    lateinit var remindersRepo: RemindersLocalRepository

    private val firstReminder = ReminderDTO("Test1", "Test1 desc", "Test1 loc", 1.1, 1.0, "test1")
    private val secondReminder = ReminderDTO("Test2", "Test2 desc", "Test2 loc", 2.2, 2.0, "test2")

    @Before
    fun createRepository() {
        remindersDao = RemindersFakeDao(mutableListOf(firstReminder, secondReminder))
        remindersRepo = RemindersLocalRepository(remindersDao, Dispatchers.Main)
    }

    @Test
    fun getReminders_returnGivenReminders() = mainCoroutineRule.runBlockingTest {
        val result = remindersRepo.getReminders()
        val reminders = mutableListOf<ReminderDTO>()

        if (result is Result.Success) {
            reminders.addAll(result.data)
        }

        assertThat(reminders[0].id, `is`(firstReminder.id))
        assertThat(reminders[0].title, `is`(firstReminder.title))
        assertThat(reminders[0].description, `is`(firstReminder.description))
        assertThat(reminders[0].location, `is`(firstReminder.location))
        assertThat(reminders[0].latitude, `is`(firstReminder.latitude))
        assertThat(reminders[0].longitude, `is`(firstReminder.longitude))

        assertThat(reminders[1].id, `is`(secondReminder.id))
        assertThat(reminders[1].title, `is`(secondReminder.title))
        assertThat(reminders[1].description, `is`(secondReminder.description))
        assertThat(reminders[1].location, `is`(secondReminder.location))
        assertThat(reminders[1].latitude, `is`(secondReminder.latitude))
        assertThat(reminders[1].longitude, `is`(secondReminder.longitude))
    }

    @Test
    fun getReminder_returnGivenReminder() = mainCoroutineRule.runBlockingTest {
        val result = remindersRepo.getReminder(firstReminder.id)
        var reminder: ReminderDTO? = null

        if (result is Result.Success) {
            reminder = result.data
        }

        assertThat(reminder?.id, `is`(firstReminder.id))
        assertThat(reminder?.title, `is`(firstReminder.title))
        assertThat(reminder?.description, `is`(firstReminder.description))
        assertThat(reminder?.location, `is`(firstReminder.location))
        assertThat(reminder?.latitude, `is`(firstReminder.latitude))
        assertThat(reminder?.longitude, `is`(firstReminder.longitude))
    }

    @Test
    fun saveReminder_getReminder_returnSavedReminder() = mainCoroutineRule.runBlockingTest {
        val thirdReminder = ReminderDTO("Test3", "Test3 desc", "Test3 loc", 3.3, 3.0, "test3")

        remindersRepo.saveReminder(thirdReminder)

        val result = remindersRepo.getReminder(thirdReminder.id)
        var reminder: ReminderDTO? = null

        if (result is Result.Success) {
            reminder = result.data
        }

        assertThat(reminder?.id, `is`(thirdReminder.id))
        assertThat(reminder?.title, `is`(thirdReminder.title))
        assertThat(reminder?.description, `is`(thirdReminder.description))
        assertThat(reminder?.location, `is`(thirdReminder.location))
        assertThat(reminder?.latitude, `is`(thirdReminder.latitude))
        assertThat(reminder?.longitude, `is`(thirdReminder.longitude))
    }

    @Test
    fun deleteAllReminders_noRemindersReturned() = mainCoroutineRule.runBlockingTest {
        remindersRepo.deleteAllReminders()

        val result = remindersRepo.getReminders()
        val reminders = mutableListOf<ReminderDTO>()

        if (result is Result.Success) {
            reminders.addAll(result.data)
        }

        assertTrue(reminders.isEmpty())
    }

    @Test
    fun onNonExistingId_getReminderById_returnError() = mainCoroutineRule.runBlockingTest {
        val result = remindersRepo.getReminder("non existing")

        assertTrue(result is Result.Error)
    }

}