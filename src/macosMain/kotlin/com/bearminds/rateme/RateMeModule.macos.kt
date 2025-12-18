package com.bearminds.rateme

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * macOS Koin module for RateMe services.
 */
actual val rateMeModule: Module = module {
    single<RateMePreferences> {
        MacosRateMePreferences()
    }

    single<EmailService> {
        MacosEmailService()
    }

    single<ReviewService> {
        MacosReviewService()
    }
}
