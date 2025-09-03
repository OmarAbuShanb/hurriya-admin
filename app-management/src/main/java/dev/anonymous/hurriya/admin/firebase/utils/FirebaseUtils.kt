package dev.anonymous.hurriya.admin.firebase.utils

import kotlin.concurrent.Volatile

class FirebaseUtils private constructor() {
    fun getFirebaseErrorMessage(errorCode: String): String {
        val errorMessage = when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email address."
            "ERROR_WRONG_PASSWORD" -> "Incorrect password."
            "ERROR_USER_NOT_FOUND" -> "User not found."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use."
            else -> "Authentication failed."
        }
        return errorMessage
    }

    companion object {
        @get:Synchronized
        @Volatile
        var instance: FirebaseUtils? = null
            get() {
                if (field == null) {
                    field = FirebaseUtils()
                }
                return field!!
            }
            private set
    }
}