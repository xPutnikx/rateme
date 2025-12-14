package com.bearminds.rateme

import java.util.prefs.Preferences

/**
 * JVM/Desktop implementation of RateMePreferences using Java Preferences API.
 */
class JvmRateMePreferences : RateMePreferences {

    private val prefs: Preferences = Preferences.userNodeForPackage(JvmRateMePreferences::class.java)

    // In-memory pending trigger (doesn't need to survive app restart)
    private var pendingTrigger: String? = null

    override suspend fun getStatus(): RateMeStatus {
        val value = prefs.get(KEY_STATUS, RateMeStatus.NOT_SHOWN.name)
        return try {
            RateMeStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RateMeStatus.NOT_SHOWN
        }
    }

    override suspend fun setStatus(status: RateMeStatus) {
        prefs.put(KEY_STATUS, status.name)
    }

    override suspend fun shouldShowRateMe(): Boolean {
        return getStatus() == RateMeStatus.NOT_SHOWN
    }

    override fun setPendingTrigger(trigger: String) {
        pendingTrigger = trigger
    }

    override fun consumePendingTrigger(): String? {
        val trigger = pendingTrigger
        pendingTrigger = null
        return trigger
    }

    companion object {
        private const val KEY_STATUS = "rate_me_status"
    }
}
