package com.example.testproject // ← Твой пакет!

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val level: Int = 1,
    val totalSolved: Int = 0,
    val joinDate: Long = System.currentTimeMillis()
)

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Получить профиль текущего пользователя
    suspend fun getProfile(): Result<UserProfile> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Не авторизован"))
            }

            val uid = currentUser.uid
            val document = firestore.collection("users").document(uid).get().await()

            if (document.exists()) {
                // Вручную извлекаем данные из документа
                val data = document.data
                if (data != null) {
                    val profile = UserProfile(
                        uid = uid,
                        email = currentUser.email ?: "",
                        displayName = data["displayName"] as? String ?: currentUser.email?.substringBefore("@") ?: "Пользователь",
                        bio = data["bio"] as? String ?: "",
                        avatarUrl = data["avatarUrl"] as? String ?: "",
                        level = (data["level"] as? Long)?.toInt() ?: 1,
                        totalSolved = (data["totalSolved"] as? Long)?.toInt() ?: 0,
                        joinDate = data["joinDate"] as? Long ?: System.currentTimeMillis()
                    )
                    Result.success(profile)
                } else {
                    createAndReturnProfile(uid, currentUser.email ?: "")
                }
            } else {
                createAndReturnProfile(uid, currentUser.email ?: "")
            }
        } catch (e: Exception) {
            // Если ошибка — возвращаем базовый профиль
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Result.success(UserProfile(
                    uid = currentUser.uid,
                    email = currentUser.email ?: "",
                    displayName = currentUser.email?.substringBefore("@") ?: "Пользователь"
                ))
            } else {
                Result.failure(e)
            }
        }
    }

    // Обновить аватар
    suspend fun updateAvatar(avatarUrl: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Не авторизован"))
            firestore.collection("users").document(uid)
                .update("avatarUrl", avatarUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createAndReturnProfile(uid: String, email: String): Result<UserProfile> {
        return try {
            val newProfile = mapOf(
                "displayName" to email.substringBefore("@"),
                "bio" to "",
                "level" to 1,
                "totalSolved" to 0,
                "joinDate" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(newProfile).await()

            Result.success(UserProfile(
                uid = uid,
                email = email,
                displayName = email.substringBefore("@"),
                bio = ""
            ))
        } catch (e: Exception) {
            // Даже если Firestore не сработал — отдаём профиль
            Result.success(UserProfile(
                uid = uid,
                email = email,
                displayName = email.substringBefore("@")
            ))
        }
    }

    // Обновить профиль
    suspend fun updateProfile(displayName: String, bio: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Не авторизован"))

            val updates = mapOf(
                "displayName" to displayName,
                "bio" to bio
            )

            firestore.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Обновить пароль
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            auth.currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Выход
    fun logout() {
        auth.signOut()
    }

    // Удаление аккаунта
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Не авторизован"))
            try {
                firestore.collection("users").document(uid).delete().await()
            } catch (_: Exception) { }
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Обновить статистику в Firestore
    // Обновить статистику в Firestore
    suspend fun updateStats(level: Int, totalSolved: Int): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Не авторизован"))

            val updates = mapOf(
                "level" to level,
                "totalSolved" to totalSolved
            )

            firestore.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Если документ не существует — создаём
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    return Result.failure(Exception("Не авторизован"))
                }

                val newProfile = mapOf(
                    "displayName" to (currentUser.email?.substringBefore("@") ?: "Пользователь"),
                    "bio" to "",
                    "level" to level,
                    "totalSolved" to totalSolved,
                    "joinDate" to System.currentTimeMillis()
                )
                firestore.collection("users").document(currentUser.uid).set(newProfile).await()
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }
}