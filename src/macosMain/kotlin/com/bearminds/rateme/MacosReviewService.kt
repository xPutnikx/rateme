package com.bearminds.rateme

import platform.StoreKit.SKStoreReviewController

/**
 * macOS implementation of ReviewService using SKStoreReviewController.
 * macOS uses the same StoreKit API as iOS for requesting reviews.
 */
class MacosReviewService : ReviewService {

    override suspend fun requestReview(platformContext: Any?): ReviewResult {
        return try {
            // macOS uses the non-scene version of requestReview
            @Suppress("DEPRECATION")
            SKStoreReviewController.requestReview()
            ReviewResult.Requested
        } catch (e: Exception) {
            ReviewResult.Failed(e)
        }
    }
}
