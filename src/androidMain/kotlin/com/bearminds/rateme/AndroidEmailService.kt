package com.bearminds.rateme

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/**
 * Android implementation of EmailService using Android Intents.
 */
class AndroidEmailService(private val context: Context) : EmailService {

    override fun openEmailComposer(email: String, subject: String, body: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            if (body.isNotEmpty()) {
                putExtra(Intent.EXTRA_TEXT, body)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
