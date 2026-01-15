package com.bearminds.rateme

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

/**
 * Android implementation of ReviewService using Google Play In-App Review API.
 *
 * IMPORTANT: This API has limitations:
 * - Must use a personal @gmail.com account (GSuite/enterprise accounts don't work)
 * - User must have downloaded the app from Play Store at least once
 * - If user already reviewed, the dialog won't show (need to delete review first)
 * - Sometimes need to clear Play Store cache/data
 * - The API is fire-and-forget - it reports success even when dialog doesn't show
 * - Limited to ~3 prompts per year per user
 */
class AndroidReviewService : ReviewService {

    override suspend fun requestReview(platformContext: Any?): ReviewResult {
        val activity = platformContext as? Activity
            ?: return ReviewResult.Failed(IllegalArgumentException("Activity required for Android review"))

        return try {
            val manager = ReviewManagerFactory.create(activity)
            val reviewInfo = manager.requestReviewFlow().await()
            manager.launchReviewFlow(activity, reviewInfo).await()
            ReviewResult.Requested
        } catch (e: Exception) {
            ReviewResult.Failed(e)
        }
    }
}
