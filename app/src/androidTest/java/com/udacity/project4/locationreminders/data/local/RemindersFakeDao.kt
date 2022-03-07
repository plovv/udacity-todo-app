package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

class RemindersFakeDao(private val reminders: MutableList<ReminderDTO>): RemindersDao {

    override suspend fun getReminders(): List<ReminderDTO> {
        return reminders
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        return reminders.first { it.id == reminderId }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}