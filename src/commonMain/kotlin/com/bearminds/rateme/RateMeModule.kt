package com.bearminds.rateme

import org.koin.core.module.Module

/**
 * Koin module for the RateMe library.
 * Platform-specific implementations provide the actual services.
 */
expect val rateMeModule: Module
