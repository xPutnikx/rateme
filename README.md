# RateMe My Friend

A Kotlin Multiplatform library for prompting users to rate your app. Provides a customizable star rating card that routes positive ratings (4-5 stars) to native app store reviews and negative ratings (1-3 stars) to email feedback.

## Platforms Supported

| Platform    | Review API                   | Email API                          | Preferences          |
|-------------|------------------------------|------------------------------------|----------------------|
| Android     | Google Play In-App Review    | Intent.ACTION_SENDTO               | SharedPreferences    |
| iOS         | SKStoreReviewController      | URL schemes (Gmail, Outlook, Mail) | NSUserDefaults       |
| JVM/Desktop | Thank-you message (no store) | java.awt.Desktop.mail()            | Java Preferences API |

## Installation

### Option 1: Git Submodule (Recommended)

Add as a git submodule:

```bash
git submodule add https://github.com/xPutnikx/rateme.git rateme
```

Add to your `settings.gradle.kts`:

```kotlin
include(":rateme")
```

Add the dependency in your app module:

```kotlin
implementation(project(":rateme"))
```

### Option 2: Copy Module

Copy the `rateme` directory into your project and add to `settings.gradle.kts`:

```kotlin
include(":rateme")
```

Add the dependency:

```kotlin
implementation(project(":rateme"))
```

### Required Version Catalog Entries

When included as a submodule, rateme uses your project's `gradle/libs.versions.toml`. Add these entries:

```toml
[versions]
kotlin = "2.2.20"
coroutines = "1.10.2"
koin = "4.1.1"
playReview = "2.0.2"

[libraries]
kotlin-stdlib = { group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version.ref = "kotlin" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-play-services = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services", version.ref = "coroutines" }
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
play-review-ktx = { module = "com.google.android.play:review-ktx", version.ref = "playReview" }
```

## Setup

### 1. Register the Koin Module

Add `rateMeModule` to your Koin configuration:

```kotlin
startKoin {
    modules(
        // ... your other modules
        rateMeModule
    )
}
```

### 2. iOS URL Schemes (Optional)

To support Gmail and Outlook on iOS, add these URL schemes to your `Info.plist`:

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
    <string>googlegmail</string>
    <string>ms-outlook</string>
</array>
```

## Usage

### Basic Usage

```kotlin
@Composable
fun SettingsScreen() {
    val preferences: RateMePreferences = koinInject()
    var shouldShow by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        shouldShow = preferences.shouldShowRateMe()
    }

    if (shouldShow) {
        RateMeCard(
            title = "Enjoying the app?",
            description = "Tap a star to rate your experience",
            thankYouTitle = "Thank you!",
            thankYouDescription = "We appreciate your support!",
            dismissButtonText = "Not now",
            okButtonText = "OK",
            config = RateMeConfig(
                feedbackEmail = "support@yourapp.com",
                feedbackSubject = "App Feedback"
            )
        )
    }
}
```

### With Callbacks

```kotlin
RateMeCard(
    title = "Enjoying the app?",
    description = "Tap a star to rate your experience",
    thankYouTitle = "Thank you!",
    thankYouDescription = "We appreciate your support!",
    dismissButtonText = "Not now",
    okButtonText = "OK",
    config = RateMeConfig(
        feedbackEmail = "support@yourapp.com",
        feedbackSubject = "App Feedback",
        onRatingSubmitted = { stars ->
            analytics.logEvent("rating_submitted", mapOf("stars" to stars))
        },
        onReviewRequested = {
            analytics.logEvent("store_review_requested")
        },
        onFeedbackRequested = {
            analytics.logEvent("email_feedback_opened")
        },
        onDismissed = {
            analytics.logEvent("rating_dismissed")
        }
    )
)
```

### Custom Star Styling

```kotlin
RateMeCard(
    // ... other params
    inactiveStyle = StarStyle(
        color = Color(0xFFE0E0E0),
        starSize = 48.dp,
        borderColor = Color(0xFFBDBDBD),
        backgroundColor = Color.Transparent
    ),
    activeStyle = StarStyle(
        color = Color(0xFFFF9800),
        starSize = 48.dp,
        borderColor = Color(0xFFFF9800),
        backgroundColor = Color.Transparent
    )
)
```

### Standalone Star Rating

Use the star rating component independently:

```kotlin
StarRatingRow(
    modifier = Modifier.fillMaxWidth(),
    initialRating = 0,
    maxRating = 5,
    onRatingChange = { rating -> /* handle rating */ },
    inactiveStyle = StarStyle(color = Color.Gray, starSize = 32.dp),
    activeStyle = StarStyle(color = Color(0xFFFFD700), starSize = 32.dp)
)
```

## Architecture

```
rateme/
├── src/
│   ├── commonMain/kotlin/com/bearminds/rateme/
│   │   ├── RateMeCard.kt          # Main composable component
│   │   ├── StarRating.kt          # Star rating UI component
│   │   ├── RateMeConfig.kt        # Configuration data class
│   │   ├── ReviewService.kt       # Interface for native reviews
│   │   ├── EmailService.kt        # Interface for email composer
│   │   ├── RateMePreferences.kt   # Interface for persistence
│   │   ├── PlatformContext.kt     # expect fun for platform context
│   │   └── RateMeModule.kt        # expect val for Koin module
│   │
│   ├── androidMain/kotlin/com/bearminds/rateme/
│   │   ├── AndroidReviewService.kt     # Google Play In-App Review
│   │   ├── AndroidEmailService.kt      # Intent-based email
│   │   ├── AndroidRateMePreferences.kt # SharedPreferences
│   │   ├── PlatformContext.android.kt  # Returns Activity
│   │   └── RateMeModule.android.kt     # Android DI module
│   │
│   ├── iosMain/kotlin/com/bearminds/rateme/
│   │   ├── IosReviewService.kt         # SKStoreReviewController
│   │   ├── IosEmailService.kt          # URL scheme email
│   │   ├── IosRateMePreferences.kt     # NSUserDefaults
│   │   ├── PlatformContext.ios.kt      # Returns null
│   │   └── RateMeModule.ios.kt         # iOS DI module
│   │
│   └── jvmMain/kotlin/com/bearminds/rateme/
│       ├── JvmReviewService.kt         # Returns NotSupported
│       ├── JvmEmailService.kt          # Desktop.mail()
│       ├── JvmRateMePreferences.kt     # Java Preferences
│       └── RateMeModule.jvm.kt         # JVM DI module
```

## API Reference

### RateMeCard

Main composable component for the rate-me flow.

| Parameter | Type | Description |
|-----------|------|-------------|
| `modifier` | `Modifier` | Modifier for the card container |
| `title` | `String` | Card title text |
| `description` | `String` | Card description/prompt text |
| `config` | `RateMeConfig` | Configuration including callbacks and email |
| `thankYouTitle` | `String` | Title for JVM thank-you view |
| `thankYouDescription` | `String` | Description for JVM thank-you view |
| `dismissButtonText` | `String` | Text for dismiss button |
| `okButtonText` | `String` | Text for OK button in thank-you view |
| `inactiveStyle` | `StarStyle` | Style for unselected stars |
| `activeStyle` | `StarStyle` | Style for selected stars |

### RateMeConfig

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `feedbackEmail` | `String` | required | Email for negative feedback |
| `feedbackSubject` | `String` | "App Feedback" | Email subject line |
| `onRatingSubmitted` | `(Int) -> Unit` | `{}` | Called when user selects rating |
| `onReviewRequested` | `() -> Unit` | `{}` | Called after native review triggered |
| `onFeedbackRequested` | `() -> Unit` | `{}` | Called after email composer opened |
| `onDismissed` | `() -> Unit` | `{}` | Called when user dismisses |

### StarStyle

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `color` | `Color` | Gray | Fill color for stars |
| `starSize` | `Dp` | 40.dp | Size of each star |
| `borderColor` | `Color` | Gray | Border/outline color |
| `backgroundColor` | `Color` | Gray | Background color |

### RateMeStatus

Enum for tracking user interaction state:

| Value | Description |
|-------|-------------|
| `NOT_SHOWN` | User hasn't interacted yet |
| `DISMISSED` | User dismissed without rating |
| `RATED_POSITIVE` | User gave 4-5 stars |
| `RATED_NEGATIVE` | User gave 1-3 stars |

### ReviewResult

Sealed interface for review request outcomes:

| Type | Description |
|------|-------------|
| `Requested` | Review prompt was triggered |
| `Failed(error)` | Review request failed |
| `NotSupported` | Platform doesn't support native reviews |

## User Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      RateMeCard                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              "Enjoying the app?"                      │  │
│  │         "Tap a star to rate your experience"         │  │
│  │                                                       │  │
│  │              ★ ★ ★ ★ ★  (5 stars)                    │  │
│  │                                                       │  │
│  │                              [Not now]                │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
            ▼              ▼              ▼
      [1-3 stars]    [4-5 stars]    [Dismiss]
            │              │              │
            ▼              ▼              ▼
    Open Email      Native Review    Hide Card
    Composer        (or Thank You    (status:
    (status:        on JVM)          DISMISSED)
    RATED_NEGATIVE) (status:
                    RATED_POSITIVE)
```

## License

MIT License
