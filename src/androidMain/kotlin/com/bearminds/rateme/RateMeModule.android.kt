package com.bearminds.rateme

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android Koin module for RateMe services.
 */
actual val rateMeModule: Module = module {
    single<RateMePreferences> {
        AndroidRateMePreferences(androidContext())
    }

    single<EmailService> {
        AndroidEmailService(androidContext())
    }

    single<ReviewService> {
        AndroidReviewService()
    }
}
