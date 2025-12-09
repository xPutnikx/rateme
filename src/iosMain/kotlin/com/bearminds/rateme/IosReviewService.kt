package com.bearminds.rateme

import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import platform.UIKit.UISceneActivationStateForegroundActive

/**
 * iOS implementation of ReviewService using SKStoreReviewController.
 */
class IosReviewService : ReviewService {

    override suspend fun requestReview(platformContext: Any?): ReviewResult {
        return try {
            // Get the active window scene for iOS 14+
            val windowScene = UIApplication.sharedApplication.connectedScenes
                .filterIsInstance<UIWindowScene>()
                .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }

            if (windowScene != null) {
                SKStoreReviewController.requestReviewInScene(windowScene)
            } else {
                // Fallback - this may not work on newer iOS versions
                @Suppress("DEPRECATION")
                SKStoreReviewController.requestReview()
            }
            ReviewResult.Requested
        } catch (e: Exception) {
            ReviewResult.Failed(e)
        }
    }
}
