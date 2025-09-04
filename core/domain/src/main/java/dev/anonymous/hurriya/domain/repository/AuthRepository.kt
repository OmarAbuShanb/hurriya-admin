package dev.anonymous.hurriya.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun registerWithInvite(
        email: String,
        password: String,
        name: String,
        inviteCode: String
    ): Result<String>
    suspend fun signOut()
}