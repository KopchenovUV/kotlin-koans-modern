package com.example.testproject

import android.content.Context
import android.content.SharedPreferences

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val condition: (solvedCount: Int) -> Boolean
)

class AchievementsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)

    // Список всех достижений
    val achievements = listOf(
        Achievement("first_step", "Первый шаг", "Решить первую задачу", "👶") { it >= 1 },
        Achievement("five_solved", "Новичок", "Решить 5 задач", "🌟") { it >= 5 },
        Achievement("ten_solved", "Знаток", "Решить 10 задач", "🎓") { it >= 10 },
        Achievement("twenty_solved", "Мастер", "Решить 20 задач", "🏆") { it >= 20 },
        Achievement("thirty_solved", "Эксперт", "Решить 30 задач", "👑") { it >= 30 },
        Achievement("all_solved", "Легенда", "Решить все 42 задачи", "💎") { it >= 42 },
        Achievement("speed_demon", "Спидраннер", "Решить 3 задачи за день", "⚡") { false }, // Пока заглушка
        Achievement("perfectionist", "Перфекционист", "Решить задачу с первой попытки", "🎯") { false }
    )

    // Получить полученные достижения
    fun getUnlockedAchievements(): List<Achievement> {
        return achievements.filter { prefs.getBoolean("ach_${it.id}", false) }
    }

    // Проверить и разблокировать новые достижения
    fun checkAndUnlock(solvedCount: Int): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        for (ach in achievements) {
            if (!prefs.getBoolean("ach_${ach.id}", false) && ach.condition(solvedCount)) {
                prefs.edit().putBoolean("ach_${ach.id}", true).apply()
                newlyUnlocked.add(ach)
            }
        }
        return newlyUnlocked
    }

    // Количество разблокированных достижений
    fun getUnlockedCount(): Int = getUnlockedAchievements().size

    // Общее количество достижений
    fun getTotalCount(): Int = achievements.size
}