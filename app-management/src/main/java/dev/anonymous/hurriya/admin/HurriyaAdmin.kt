package dev.anonymous.hurriya.admin

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import dev.anonymous.hurriya.admin.domain.usecase.TrackPresenceUseCase
import javax.inject.Inject

@HiltAndroidApp
class HurriyaAdmin : Application(), FirebaseAuth.AuthStateListener {
    @Inject
    lateinit var trackPresenceUseCase: TrackPresenceUseCase

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        auth.addAuthStateListener(this)
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        if (auth.currentUser != null) {
            trackPresenceUseCase()
        }
    }
}
