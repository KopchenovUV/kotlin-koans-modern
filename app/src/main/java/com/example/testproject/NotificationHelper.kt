package com.example.testproject // Не забудь заменить на свой пакет!

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

object NotificationHelper {

    private const val CHANNEL_ID = "kotlin_koans_reminder"
    private const val CHANNEL_NAME = "Напоминания о тренировках"
    private const val CHANNEL_DESCRIPTION = "Уведомления, которые помогут не забыть про Kotlin"

    // Список завлекающих сообщений
    private val reminderMessages = listOf(
        "🦸‍♂️ Даже супергерои учатся! Жми и проверь свои силы в Kotlin!",
        "🧠 Мозг требует разминки. Пара задач — и ты в форме!",
        "☕️ 5 минут на Kotlin заменяют чашку кофе. Взбодрись кодом!",
        "💎 Говорят, эксперты Kotlin на вес золота. Добудь свой грамм сегодня!",
        "🏆 Твой рейтинг сам себя не поднимет! Вперёд, к новым ачивкам!",
        "🚀 3... 2... 1... Пуск! Твой код ждёт запуска.",
        "🤖 Даже роботы учат Kotlin. А ты чем хуже?",
        "🎯 Цель на сегодня: стать на 1% лучше в Kotlin. Выполнима!",
        "🪄 Преврати «ничего не понимаю» в «да это же просто!» за пару задач.",
        "🗺️ Твой путь в Android-разработку начинается с одного тапа.",
        "🥱 Скучно? Мы подготовили для тебя задачку!",
        "🦄 Найди своего единорога в мире Kotlin. Жми!"
    )

    fun createNotificationChannel(context: Context) {
        // Создаём канал уведомлений, если его ещё нет
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // HIGHT - со звуком и вибрацией
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminder(context: Context) {
        // Выбираем случайную мотивирующую фразу
        val message = reminderMessages.random()

        // Создаём Intent для открытия приложения при нажатии
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Строим уведомление
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Стандартная иконка
            .setContentTitle("Kotlin Koans")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Показываем уведомление
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(Random.nextInt(1000), builder.build())
            }
        } catch (e: SecurityException) {
            // Разрешение не выдано — игнорируем
            e.printStackTrace()
        }
    }
}