package com.example.testproject // ← Твой пакет!

import android.content.Context
import android.content.SharedPreferences

class ProgressManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("koans_progress", Context.MODE_PRIVATE)

    // Отметить задачу как решённую
    fun markChallengeSolved(challengeId: Int) {
        prefs.edit().putBoolean("solved_$challengeId", true).apply()
    }

    // Проверить, решена ли задача
    fun isChallengeSolved(challengeId: Int): Boolean {
        return prefs.getBoolean("solved_$challengeId", false)
    }

    // Получить количество решённых задач
    fun getSolvedCount(): Int {
        var count = 0
        prefs.all.keys.forEach { key ->
            if (key.startsWith("solved_") && prefs.getBoolean(key, false)) {
                count++
            }
        }
        return count
    }

    // Получить список ID решённых задач
    fun getSolvedIds(): List<Int> {
        val ids = mutableListOf<Int>()
        prefs.all.keys.forEach { key ->
            if (key.startsWith("solved_") && prefs.getBoolean(key, false)) {
                val id = key.removePrefix("solved_").toIntOrNull()
                if (id != null) ids.add(id)
            }
        }
        return ids
    }

    // Сохранить код пользователя для задачи
    fun saveUserCode(challengeId: Int, code: String) {
        prefs.edit().putString("code_$challengeId", code).apply()
    }

    // Получить сохранённый код пользователя
    fun getUserCode(challengeId: Int): String? {
        return prefs.getString("code_$challengeId", null)
    }

    // Сбросить весь прогресс
    fun resetAllProgress() {
        prefs.edit().clear().apply()
    }
}