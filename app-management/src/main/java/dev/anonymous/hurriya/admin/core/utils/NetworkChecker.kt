package dev.anonymous.hurriya.admin.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dev.anonymous.hurriya.admin.core.di.FirebaseModule

class NetworkChecker(private val context: Context) {
    fun isOnline(): Boolean {
        if (FirebaseModule.USE_EMULATOR) return true
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
