package com.bearminds.rateme

/**
 * State of the rate-me flow for persistence.
 */
enum class RateMeStatus {
    /** User hasn't interacted yet */
    NOT_SHOWN,

    /** User dismissed without rating */
    DISMISSED,

    /** User gave positive rating (4-5 stars) */
    RATED_POSITIVE,

    /** User gave negative rating (1-3 stars) and was directed to email */
    RATED_NEGATIVE
}

/**
 * Persistence interface for rate-me state.
 *
 * Platform implementations use native preferences:
 * - Android: SharedPreferences
 * - iOS: NSUserDefaults
 * - JVM: Java Preferences API
 */
interface RateMePreferences {
    /**
     * Get the current rate-me status.
     */
    suspend fun getStatus(): RateMeStatus

    /**
     * Set the rate-me status.
     */
    suspend fun setStatus(status: RateMeStatus)

    /**
     * Check if the rate-me card should be shown.
     * Returns true only if user hasn't already rated or dismissed.
     */
    suspend fun shouldShowRateMe(): Boolean
}
