package dev.anonymous.hurriya.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import dev.anonymous.hurriya.core.exceptions.InvalidInviteResponseException
import dev.anonymous.hurriya.core.exceptions.UserDataException
import dev.anonymous.hurriya.data.remote.keys.functions.CloudFunctionNames
import dev.anonymous.hurriya.data.remote.keys.functions.RegisterWithInviteFunctionFields
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions
) {
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password)
                .await()

            val uid = authResult.user?.uid
                ?: return Result.failure(UserDataException())

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithInvite(
        email: String,
        password: String,
        name: String,
        inviteCode: String
    ): Result<String> {
        return try {
            val data = hashMapOf(
                RegisterWithInviteFunctionFields.Request.EMAIL to email,
                RegisterWithInviteFunctionFields.Request.PASSWORD to password,
                RegisterWithInviteFunctionFields.Request.NAME to name,
                RegisterWithInviteFunctionFields.Request.CODE to inviteCode
            )
            val result = functions
                .getHttpsCallable(CloudFunctionNames.REGISTER_WITH_INVITE)
                .call(data)
                .await()

            val map = result.data as? Map<*, *>
            val role = map?.get(RegisterWithInviteFunctionFields.Response.ROLE) as? String

            if (role != null) {
                Result.success(role)
            } else {
                Result.failure(InvalidInviteResponseException())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
