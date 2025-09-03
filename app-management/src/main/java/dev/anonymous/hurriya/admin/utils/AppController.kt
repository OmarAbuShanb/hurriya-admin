package dev.anonymous.hurriya.admin.utils

import android.app.Application
import kotlin.concurrent.Volatile

class AppController : Application() {
    override fun onCreate() {
        super.onCreate()
        Instance = this
    }

    companion object {
        @Volatile
        private var Instance: AppController? = null

        fun getInstance(): AppController? {
            if (Instance != null) {
                return Instance
            }
            return null
        }
    }
}