package com.bearminds.rateme

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS Koin module for RateMe services.
 */
actual val rateMeModule: Module = module {
    single<RateMePreferences> {
        IosRateMePreferences()
    }

    single<EmailService> {
        IosEmailService()
    }

    single<ReviewService> {
        IosReviewService()
    }
}
