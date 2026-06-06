package com.example.testproject // Не забудь заменить!

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Просто вызываем наш хелпер для показа уведомления
        NotificationHelper.showReminder(applicationContext)
        return Result.success()
    }
}