package com.bearminds.rateme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/**
 * Internal state for the RateMeCard flow.
 */
private enum class RateMeCardState {
    /** Initial state - showing star rating */
    RATING,
    /** User gave positive rating on JVM - showing thank you */
    THANK_YOU,
    /** Flow completed - card should be hidden */
    COMPLETED
}

/**
 * A reusable rate-me card component with star rating.
 *
 * User flow:
 * - 4-5 stars: Triggers native in-app review (Google Play / App Store) or shows thank you on JVM
 * - 1-3 stars: Opens email composer for feedback
 *
 * The component automatically tracks if user has already rated/dismissed
 * and will not show again after completion.
 *
 * @param modifier Modifier for the card container
 * @param title Card title text
 * @param description Card description/prompt text
 * @param config Configuration including callbacks and email settings
 * @param thankYouTitle Title shown on JVM after positive rating
 * @param thankYouDescription Description shown on JVM after positive rating
 * @param dismissButtonText Text for the dismiss button
 * @param okButtonText Text for the OK button in thank you view
 * @param inactiveStyle Style for stars when no rating is selected
 * @param activeStyle Style for stars when a rating is selected
 */
@Composable
fun RateMeCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    config: RateMeConfig,
    thankYouTitle: String,
    thankYouDescription: String,
    dismissButtonText: String,
    okButtonText: String,
    inactiveStyle: StarStyle = StarStyle(
        color = Color(0xFFE0E0E0),
        starSize = 40.dp,
        borderColor = Color(0xFFE0E0E0),
        backgroundColor = Color.Transparent
    ),
    activeStyle: StarStyle = StarStyle(
        color = Color(0xFFFFD700),
        starSize = 40.dp,
        borderColor = Color(0xFFFFD700),
        backgroundColor = Color.Transparent
    )
) {
    val reviewService: ReviewService = koinInject()
    val emailService: EmailService = koinInject()
    val preferences: RateMePreferences = koinInject()

    // Automatically get platform context (Activity on Android)
    val platformContext = getPlatformContext()

    val scope = rememberCoroutineScope()

    var cardState by remember { mutableStateOf(RateMeCardState.RATING) }
    var currentRating by remember { mutableIntStateOf(0) }

    // Hide card if flow is completed
    if (cardState == RateMeCardState.COMPLETED) {
        return
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (cardState) {
                RateMeCardState.RATING -> {
                    RatingContent(
                        title = title,
                        description = description,
                        currentRating = currentRating,
                        onRatingChanged = { rating ->
                            currentRating = rating
                            config.onRatingSubmitted(rating)

                            scope.launch {
                                handleRating(
                                    rating = rating,
                                    config = config,
                                    reviewService = reviewService,
                                    emailService = emailService,
                                    preferences = preferences,
                                    platformContext = platformContext,
                                    onStateChange = { cardState = it }
                                )
                            }
                        },
                        dismissButtonText = dismissButtonText,
                        onDismiss = {
                            scope.launch {
                                preferences.setStatus(RateMeStatus.DISMISSED)
                            }
                            config.onDismissed()
                            cardState = RateMeCardState.COMPLETED
                        },
                        inactiveStyle = inactiveStyle,
                        activeStyle = activeStyle
                    )
                }

                RateMeCardState.THANK_YOU -> {
                    ThankYouContent(
                        title = thankYouTitle,
                        description = thankYouDescription,
                        okButtonText = okButtonText,
                        onDismiss = {
                            cardState = RateMeCardState.COMPLETED
                        }
                    )
                }

                RateMeCardState.COMPLETED -> {
                    // This case is handled by early return above
                }
            }
        }
    }
}

@Composable
private fun RatingContent(
    title: String,
    description: String,
    currentRating: Int,
    onRatingChanged: (Int) -> Unit,
    dismissButtonText: String,
    onDismiss: () -> Unit,
    inactiveStyle: StarStyle,
    activeStyle: StarStyle
) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    StarRatingRow(
        modifier = Modifier.fillMaxWidth(),
        initialRating = currentRating,
        onRatingChange = onRatingChanged,
        inactiveStyle = inactiveStyle,
        activeStyle = activeStyle
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) {
            Text(text = dismissButtonText)
        }
    }
}

@Composable
private fun ThankYouContent(
    title: String,
    description: String,
    okButtonText: String,
    onDismiss: () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = onDismiss) {
        Text(text = okButtonText)
    }
}

private suspend fun handleRating(
    rating: Int,
    config: RateMeConfig,
    reviewService: ReviewService,
    emailService: EmailService,
    preferences: RateMePreferences,
    platformContext: Any?,
    onStateChange: (RateMeCardState) -> Unit
) {
    if (rating >= 4) {
        // Positive rating - request app store review
        val result = reviewService.requestReview(platformContext)

        when (result) {
            is ReviewResult.Requested -> {
                preferences.setStatus(RateMeStatus.RATED_POSITIVE)
                config.onReviewRequested()
                onStateChange(RateMeCardState.COMPLETED)
            }

            is ReviewResult.NotSupported -> {
                // JVM/Desktop - show thank you message
                preferences.setStatus(RateMeStatus.RATED_POSITIVE)
                config.onReviewRequested()
                onStateChange(RateMeCardState.THANK_YOU)
            }

            is ReviewResult.Failed -> {
                // If review fails, still mark as positive and complete
                preferences.setStatus(RateMeStatus.RATED_POSITIVE)
                config.onReviewRequested()
                onStateChange(RateMeCardState.COMPLETED)
            }
        }
    } else {
        // Negative rating - open email for feedback
        emailService.openEmailComposer(
            email = config.feedbackEmail,
            subject = config.feedbackSubject
        )
        preferences.setStatus(RateMeStatus.RATED_NEGATIVE)
        config.onFeedbackRequested()
        onStateChange(RateMeCardState.COMPLETED)
    }
}

@Preview
@Composable
private fun RatingContent_NoSelection_Preview() {
    MaterialTheme {
        Card(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RatingContent(
                    title = "Enjoying AudioBookify?",
                    description = "Tap a star to rate your experience",
                    currentRating = 0,
                    onRatingChanged = {},
                    dismissButtonText = "Not now",
                    onDismiss = {},
                    inactiveStyle = StarStyle(
                        color = Color(0xFFE0E0E0),
                        starSize = 40.dp,
                        borderColor = Color(0xFFE0E0E0),
                        backgroundColor = Color.Transparent
                    ),
                    activeStyle = StarStyle(
                        color = Color(0xFFFFD700),
                        starSize = 40.dp,
                        borderColor = Color(0xFFFFD700),
                        backgroundColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun RatingContent_PartialSelection_Preview() {
    MaterialTheme {
        Card(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RatingContent(
                    title = "Enjoying AudioBookify?",
                    description = "Tap a star to rate your experience",
                    currentRating = 3,
                    onRatingChanged = {},
                    dismissButtonText = "Not now",
                    onDismiss = {},
                    inactiveStyle = StarStyle(
                        color = Color(0xFFE0E0E0),
                        starSize = 40.dp,
                        borderColor = Color(0xFFE0E0E0),
                        backgroundColor = Color.Transparent
                    ),
                    activeStyle = StarStyle(
                        color = Color(0xFFFFD700),
                        starSize = 40.dp,
                        borderColor = Color(0xFFFFD700),
                        backgroundColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun RatingContent_FullSelection_Preview() {
    MaterialTheme {
        Card(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RatingContent(
                    title = "Enjoying AudioBookify?",
                    description = "Tap a star to rate your experience",
                    currentRating = 5,
                    onRatingChanged = {},
                    dismissButtonText = "Not now",
                    onDismiss = {},
                    inactiveStyle = StarStyle(
                        color = Color(0xFFE0E0E0),
                        starSize = 40.dp,
                        borderColor = Color(0xFFE0E0E0),
                        backgroundColor = Color.Transparent
                    ),
                    activeStyle = StarStyle(
                        color = Color(0xFFFFD700),
                        starSize = 40.dp,
                        borderColor = Color(0xFFFFD700),
                        backgroundColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun ThankYouContent_Preview() {
    MaterialTheme {
        Card(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ThankYouContent(
                    title = "Thank you!",
                    description = "We appreciate your support!",
                    okButtonText = "OK",
                    onDismiss = {}
                )
            }
        }
    }
}
