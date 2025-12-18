package com.bearminds.rateme

import platform.Foundation.NSUserDefaults

/**
 * macOS implementation of RateMePreferences using NSUserDefaults.
 * Same implementation as iOS since NSUserDefaults API is identical.
 */
class MacosRateMePreferences : RateMePreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    // In-memory pending trigger (doesn't need to survive app restart)
    private var pendingTrigger: String? = null

    override suspend fun getStatus(): RateMeStatus {
        val value = defaults.stringForKey(KEY_STATUS)
        return if (value != null) {
            try {
                RateMeStatus.valueOf(value)
            } catch (e: IllegalArgumentException) {
                RateMeStatus.NOT_SHOWN
            }
        } else {
            RateMeStatus.NOT_SHOWN
        }
    }

    override suspend fun setStatus(status: RateMeStatus) {
        defaults.setObject(status.name, forKey = KEY_STATUS)
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
        private const val KEY_STATUS = "com.bearminds.rateme.status"
    }
}
