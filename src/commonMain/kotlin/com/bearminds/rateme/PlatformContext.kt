package com.bearminds.rateme

import androidx.compose.runtime.Composable

/**
 * Gets the platform-specific context needed for review operations.
 * - Android: Returns the current Activity
 * - iOS/JVM: Returns null (not needed)
 */
@Composable
expect fun getPlatformContext(): Any?
