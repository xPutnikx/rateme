package com.bearminds.rateme

/**
 * Interface for opening the platform email composer.
 *
 * Platform implementations:
 * - Android: Uses Intent.ACTION_SENDTO with mailto URI
 * - iOS: Tries Gmail, Outlook, then default Mail app via URL schemes
 * - JVM/Desktop: Uses java.awt.Desktop.mail()
 */
interface EmailService {
    /**
     * Open the email composer with pre-filled recipient and subject.
     *
     * @param email Recipient email address
     * @param subject Email subject line
     * @param body Optional email body text
     * @return true if email composer was opened successfully
     */
    fun openEmailComposer(email: String, subject: String, body: String = ""): Boolean
}
