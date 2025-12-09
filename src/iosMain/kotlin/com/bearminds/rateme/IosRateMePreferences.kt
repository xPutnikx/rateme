package com.bearminds.rateme

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of RateMePreferences using NSUserDefaults.
 */
class IosRateMePreferences : RateMePreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

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

    companion object {
        private const val KEY_STATUS = "com.bearminds.rateme.status"
    }
}
