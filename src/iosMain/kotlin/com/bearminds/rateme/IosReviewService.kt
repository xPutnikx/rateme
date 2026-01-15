package com.bearminds.rateme

import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import platform.UIKit.UISceneActivationStateForegroundActive

/**
 * iOS implementation of ReviewService using SKStoreReviewController.
 *
 * IMPORTANT: This API has limitations:
 * - TestFlight: The review dialog will NOT appear (Apple restriction)
 * - Production: Limited to 3 prompts per year per user
 * - The API is fire-and-forget - it reports success even when dialog doesn't show
 */
class IosReviewService : ReviewService {

    override suspend fun requestReview(platformContext: Any?): ReviewResult {
        return try {
            // Get the active window scene for iOS 14+
            // Note: filterIsInstance may not work correctly on NSSet in Kotlin/Native,
            // so we manually iterate and check types
            var activeWindowScene: UIWindowScene? = null

            for (scene in UIApplication.sharedApplication.connectedScenes) {
                if (scene is UIWindowScene && scene.activationState == UISceneActivationStateForegroundActive) {
                    activeWindowScene = scene
                    break
                }
            }

            if (activeWindowScene != null) {
                SKStoreReviewController.requestReviewInScene(activeWindowScene)
            } else {
                // Fallback - try to find any UIWindowScene
                for (scene in UIApplication.sharedApplication.connectedScenes) {
                    if (scene is UIWindowScene) {
                        SKStoreReviewController.requestReviewInScene(scene)
                        return ReviewResult.Requested
                    }
                }
                // Last resort fallback - this may not work on newer iOS versions
                @Suppress("DEPRECATION")
                SKStoreReviewController.requestReview()
            }
            ReviewResult.Requested
        } catch (e: Exception) {
            ReviewResult.Failed(e)
        }
    }
}
