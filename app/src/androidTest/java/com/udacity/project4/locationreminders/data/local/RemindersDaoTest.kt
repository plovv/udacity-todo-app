package com.udacity.project4.locationreminders.data.local

import android.text.TextUtils.isEmpty
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // Given - save reminder
        val reminder = ReminderDTO("Test1", "Test1 desc", "Test1 loc", 1.1, 1.0, "Test1Id")

        database.reminderDao().saveReminder(reminder)

        // When - retrieve reminder with id
        val retrievedReminder = database.reminderDao().getReminderById(reminder.id)

        // Then - reminder match
        assertThat(retrievedReminder?.id , `is`(reminder.id))
        assertThat(retrievedReminder?.title , `is`(reminder.title))
        assertThat(retrievedReminder?.description , `is`(reminder.description))
        assertThat(retrievedReminder?.location , `is`(reminder.location))
        assertThat(retrievedReminder?.latitude , `is`(reminder.latitude))
        assertThat(retrievedReminder?.longitude , `is`(reminder.longitude))
    }

    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        // Given - save reminder and update
        val reminder = ReminderDTO("Test1", "Test1 desc", "Test1 loc", 1.1, 1.0, "Test1Id")

        database.reminderDao().saveReminder(reminder)

        reminder.title = "Test1 updated"
        database.reminderDao().saveReminder(reminder)

        // When - retrieve reminder with id
        val retrievedReminder = database.reminderDao().getReminderById(reminder.id)

        // Then - reminder match
        assertThat(retrievedReminder?.id , `is`(reminder.id))
    }

    @Test
    fun insertRemindersAndGetReminders() = runBlockingTest {
        // Given - save reminders
        val reminder1 = ReminderDTO("Test1", "Test1 desc", "Test1 loc", 1.1, 1.0, "Test1Id")
        val reminder2 = ReminderDTO("Test2", "Test2 desc", "Test2 loc", 2.2, 2.0, "Test2Id")

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // When - retrieve reminders
        val reminders = database.reminderDao().getReminders()

        // Then - reminders match
        assertThat(reminders[0].id , `is`(reminder1.id))
        assertThat(reminders[0].title , `is`(reminder1.title))
        assertThat(reminders[0].description , `is`(reminder1.description))
        assertThat(reminders[0].location , `is`(reminder1.location))
        assertThat(reminders[0].latitude , `is`(reminder1.latitude))
        assertThat(reminders[0].longitude , `is`(reminder1.longitude))

        assertThat(reminders[1].id , `is`(reminder2.id))
        assertThat(reminders[1].title , `is`(reminder2.title))
        assertThat(reminders[1].description , `is`(reminder2.description))
        assertThat(reminders[1].location , `is`(reminder2.location))
        assertThat(reminders[1].latitude , `is`(reminder2.latitude))
        assertThat(reminders[1].longitude , `is`(reminder2.longitude))
    }

    @Test
    fun deleteRemindersAndGetEmpty() = runBlockingTest {
        // Given - save reminder and delete after
        val reminder1 = ReminderDTO("Test1", "Test1 desc", "Test1 loc", 1.1, 1.0, "Test1Id")
        val reminder2 = ReminderDTO("Test2", "Test2 desc", "Test2 loc", 2.2, 2.0, "Test2Id")

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        database.reminderDao().deleteAllReminders()

        // When - retrieve reminders
        val reminders = database.reminderDao().getReminders()

        // Then - reminders are empty
        assertTrue(reminders.isEmpty())
    }

}