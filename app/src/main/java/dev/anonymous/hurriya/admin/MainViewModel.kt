package dev.anonymous.hurriya.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import dev.anonymous.hurriya.admin.domain.usecase.auth.SignOutUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferences: UserPreferences,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    var hasSetStartDestination: Boolean = false
        private set

    fun markStartDestinationSet() {
        hasSetStartDestination = true
    }

    fun checkSuperAdmin() = userPreferences.isSuperAdmin()

    fun logoutUser() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        val hasUser = firebaseAuth.currentUser != null
        val hasStaffRole = userPreferences.isStaffRoleSet()
        return hasUser && hasStaffRole
    }
} 
