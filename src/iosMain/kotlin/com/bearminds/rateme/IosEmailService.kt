package com.bearminds.rateme

import platform.Foundation.NSURL
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

/**
 * iOS implementation of EmailService supporting multiple email clients.
 * Tries Gmail, Outlook, and default Mail app in order.
 */
class IosEmailService : EmailService {

    override fun openEmailComposer(email: String, subject: String, body: String): Boolean {
        val encodedSubject = encodeUrlComponent(subject)
        val encodedBody = encodeUrlComponent(body)

        val bodyParam = if (body.isNotEmpty()) "&body=$encodedBody" else ""

        // Try different email clients in order
        val urlStrings = listOf(
            // Gmail
            "googlegmail://co?to=$email&subject=$encodedSubject$bodyParam",
            // Outlook
            "ms-outlook://compose?to=$email&subject=$encodedSubject$bodyParam",
            // Default Mail
            "mailto:$email?subject=$encodedSubject$bodyParam"
        )

        for (urlString in urlStrings) {
            NSURL.URLWithString(urlString)?.let { url ->
                if (UIApplication.sharedApplication.canOpenURL(url)) {
                    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any?>(), null)
                    return true
                }
            }
        }

        return false
    }

    private fun encodeUrlComponent(value: String): String {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return (value as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: value.replace(" ", "%20")
    }
}
