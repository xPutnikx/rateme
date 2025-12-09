package com.bearminds.rateme

/**
 * JVM/Desktop implementation of ReviewService.
 * Desktop doesn't have native app store reviews, so this returns NotSupported.
 * The UI layer should show a thank-you message instead.
 */
class JvmReviewService : ReviewService {

    override suspend fun requestReview(platformContext: Any?): ReviewResult {
        // Desktop doesn't have native app store reviews
        return ReviewResult.NotSupported
    }
}
