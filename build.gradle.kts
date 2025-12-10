plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
}

group = "com.bearminds"
version = "0.5.1"

// Detect if running on macOS (required for iOS targets)
val isMacOS = System.getProperty("os.name").lowercase().contains("mac")

kotlin {

    jvmToolchain(21)

    jvm()

    androidTarget()

    // Only configure iOS targets on macOS
    // JitPack runs on Linux and can't build iOS artifacts
    if (isMacOS) {
        sourceSets {
            named { it.lowercase().startsWith("ios") }.configureEach {
                languageSettings {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }

        val xcfName = "ratemeKit"

        listOf(
            iosArm64(),
            iosX64(),
            iosSimulatorArm64(),
        ).forEach {
            it.binaries.framework {
                baseName = xcfName
                isStatic = true
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.components.resources)

                // Koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Google Play In-App Review
                implementation(libs.play.review.ktx)

                // Coroutines Play Services for await()
                implementation(libs.kotlinx.coroutines.play.services)

                // Koin Android
                implementation(libs.koin.android)
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
            }
        }

        if (isMacOS) {
            iosMain {
                dependencies {
                    // Uses native StoreKit - no additional dependencies
                }
            }
        }

        jvmMain {
            dependencies {
                // Uses java.awt.Desktop - no additional dependencies
            }
        }
    }
}

android {
    namespace = "com.bearminds.rateme"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // Configure Android library publishing - required for KMP Android target
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// Publishing configuration for all KMP targets
// The Kotlin plugin auto-creates publications for: jvm, iosArm64, iosX64, iosSimulatorArm64
// Android publication is configured via android.publishing block above
publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("RateMe")
                description.set("Kotlin Multiplatform library for in-app rating prompts with native app store review integration")
                url.set("https://github.com/xputnikx/rateme")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("xputnikx")
                        name.set("Vlad Hudnitsky")
                    }
                }

                scm {
                    url.set("https://github.com/xputnikx/rateme")
                    connection.set("scm:git:git://github.com/xputnikx/rateme.git")
                    developerConnection.set("scm:git:ssh://github.com/xputnikx/rateme.git")
                }
            }
        }
    }

    repositories {
        // Local maven for testing
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}