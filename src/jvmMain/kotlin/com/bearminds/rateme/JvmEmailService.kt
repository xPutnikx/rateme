package com.bearminds.rateme

import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder

/**
 * JVM/Desktop implementation of EmailService using Java Desktop API.
 */
class JvmEmailService : EmailService {

    override fun openEmailComposer(email: String, subject: String, body: String): Boolean {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
            return false
        }

        return try {
            val encodedSubject = URLEncoder.encode(subject, "UTF-8")
            val bodyParam = if (body.isNotEmpty()) {
                "&body=${URLEncoder.encode(body, "UTF-8")}"
            } else {
                ""
            }
            val mailtoUri = URI("mailto:$email?subject=$encodedSubject$bodyParam")
            Desktop.getDesktop().mail(mailtoUri)
            true
        } catch (e: Exception) {
            false
        }
    }
}
