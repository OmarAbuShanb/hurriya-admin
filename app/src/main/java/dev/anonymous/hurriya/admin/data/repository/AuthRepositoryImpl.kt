package dev.anonymous.hurriya.admin.data.repository

import dev.anonymous.hurriya.admin.data.remote.datasource.AuthRemoteDataSource
import dev.anonymous.hurriya.admin.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> {
        return authRemoteDataSource.login(email, password)
    }

    override suspend fun registerWithInvite(
        email: String,
        password: String,
        name: String,
        inviteCode: String
    ): Result<String> {
        return authRemoteDataSource.registerWithInvite(email, password, name, inviteCode)
    }

    override suspend fun signOut() {
        authRemoteDataSource.signOut()
    }
}

