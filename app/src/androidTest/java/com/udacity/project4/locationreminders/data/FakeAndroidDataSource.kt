package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidDataSource : ReminderDataSource {

    val returnErrorMessage = "Test error"
    var reminders: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(returnErrorMessage)
        }

        return Result.Success(reminders.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error(returnErrorMessage)
        }

        if (reminders.containsKey(id)) {
            return Result.Success(reminders[id]!!)
        }

        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}