package com.bearminds.rateme

import platform.AppKit.NSWorkspace
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters

/**
 * macOS implementation of EmailService using NSWorkspace.
 * Opens the default mail application via mailto: URL scheme.
 */
class MacosEmailService : EmailService {

    override fun openEmailComposer(email: String, subject: String, body: String): Boolean {
        val encodedSubject = encodeUrlComponent(subject)
        val encodedBody = encodeUrlComponent(body)

        val bodyParam = if (body.isNotEmpty()) "&body=$encodedBody" else ""

        // macOS uses mailto: URL scheme with the default mail client
        val urlString = "mailto:$email?subject=$encodedSubject$bodyParam"

        return NSURL.URLWithString(urlString)?.let { url ->
            NSWorkspace.sharedWorkspace.openURL(url)
        } ?: false
    }

    private fun encodeUrlComponent(value: String): String {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return (value as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: value.replace(" ", "%20")
    }
}
