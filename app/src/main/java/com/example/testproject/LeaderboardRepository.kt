package com.example.testproject // ← Твой пакет!

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class LeaderboardUser(
    val uid: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val totalSolved: Int = 0,
    val level: Int = 1
)

class LeaderboardRepository {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Получает топ-10 пользователей по количеству решённых задач
     */
    suspend fun getTopUsers(limit: Int = 10): Result<List<LeaderboardUser>> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("totalSolved", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                LeaderboardUser(
                    uid = doc.id,
                    displayName = data["displayName"] as? String ?: "Аноним",
                    avatarUrl = data["avatarUrl"] as? String ?: "",
                    totalSolved = (data["totalSolved"] as? Long)?.toInt() ?: 0,
                    level = (data["level"] as? Long)?.toInt() ?: 1
                )
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}