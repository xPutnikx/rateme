package com.bearminds.rateme

/**
 * Configuration for the RateMe card component.
 *
 * @param feedbackEmail Email address for negative feedback (1-3 stars)
 * @param feedbackSubject Email subject line for feedback
 * @param onRatingSubmitted Callback when user selects a star rating
 * @param onReviewRequested Callback after native review prompt is triggered (4-5 stars)
 * @param onFeedbackRequested Callback after email composer opens (1-3 stars)
 * @param onDismissed Callback when user dismisses without rating
 */
data class RateMeConfig(
    val feedbackEmail: String,
    val feedbackSubject: String = "App Feedback",
    val onRatingSubmitted: (stars: Int) -> Unit = {},
    val onReviewRequested: () -> Unit = {},
    val onFeedbackRequested: () -> Unit = {},
    val onDismissed: () -> Unit = {}
)
