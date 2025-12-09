package com.bearminds.rateme

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * JVM/Desktop Koin module for RateMe services.
 */
actual val rateMeModule: Module = module {
    single<RateMePreferences> {
        JvmRateMePreferences()
    }

    single<EmailService> {
        JvmEmailService()
    }

    single<ReviewService> {
        JvmReviewService()
    }
}
