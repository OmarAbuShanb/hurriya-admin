package dev.anonymous.hurriya.admin.domain.presence

interface PresenceManager {
    fun setupOnDisconnect()
    fun setOnline()
    fun setOffline()
}