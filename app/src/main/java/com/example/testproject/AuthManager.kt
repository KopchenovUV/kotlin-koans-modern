package com.example.testproject // ← Твой пакет!

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Текущий пользователь
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Проверка, вошёл ли пользователь
    fun isLoggedIn(): Boolean = auth.currentUser != null

    // Регистрация
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Вход
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Выход
    fun logout() {
        auth.signOut()
    }
}