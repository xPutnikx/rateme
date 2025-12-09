package com.bearminds.rateme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Android implementation of RateMePreferences using SharedPreferences.
 */
class AndroidRateMePreferences(context: Context) : RateMePreferences {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override suspend fun getStatus(): RateMeStatus {
        val value = prefs.getString(KEY_STATUS, RateMeStatus.NOT_SHOWN.name)
        return try {
            RateMeStatus.valueOf(value ?: RateMeStatus.NOT_SHOWN.name)
        } catch (_: IllegalArgumentException) {
            RateMeStatus.NOT_SHOWN
        }
    }

    override suspend fun setStatus(status: RateMeStatus) {
        prefs.edit { putString(KEY_STATUS, status.name) }
    }

    override suspend fun shouldShowRateMe(): Boolean {
        return getStatus() == RateMeStatus.NOT_SHOWN
    }

    companion object {
        private const val PREFS_NAME = "rateme_prefs"
        private const val KEY_STATUS = "rate_me_status"
    }
}
