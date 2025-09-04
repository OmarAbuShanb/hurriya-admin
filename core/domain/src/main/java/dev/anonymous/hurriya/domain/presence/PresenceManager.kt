package dev.anonymous.hurriya.domain.presence

interface PresenceManager {
    fun setupOnDisconnect()
    fun setOnline()
    fun setOffline()
}