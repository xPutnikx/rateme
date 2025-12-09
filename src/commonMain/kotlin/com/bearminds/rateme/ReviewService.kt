package com.bearminds.rateme

/**
 * Result of a review request operation.
 */
sealed interface ReviewResult {
    /** Review prompt was successfully triggered (doesn't guarantee user submitted) */
    data object Requested : ReviewResult

    /** Review request failed */
    data class Failed(val error: Throwable? = null) : ReviewResult

    /** Platform doesn't support native reviews (e.g., Desktop) */
    data object NotSupported : ReviewResult
}

/**
 * Interface for triggering native in-app review prompts.
 *
 * Platform implementations:
 * - Android: Uses Google Play In-App Review API
 * - iOS: Uses SKStoreReviewController
 * - JVM/Desktop: Returns NotSupported (show thank-you message instead)
 */
interface ReviewService {
    /**
     * Request the native in-app review dialog.
     *
     * @param platformContext Platform-specific context (Activity on Android, unused on other platforms)
     * @return [ReviewResult] indicating the outcome of the request
     */
    suspend fun requestReview(platformContext: Any? = null): ReviewResult
}
