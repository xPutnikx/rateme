package com.bearminds.rateme

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

/**
 * Android implementation of ReviewService using Google Play In-App Review API.
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
