plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
}

group = "com.bearminds"
version = "0.5.2"

kotlin {

    jvmToolchain(21)

    jvm()

    androidTarget()

    sourceSets {
        // Enable ExperimentalForeignApi for all Apple platforms
        named {
            it.lowercase().let { n -> n.startsWith("ios") || n.startsWith("macos") }
        }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }

    val xcfName = "ratemeKit"

    // iOS targets
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

    // macOS targets
    listOf(
        macosArm64(),
        macosX64(),
    ).forEach {
        it.binaries.framework {
            baseName = xcfName
            isStatic = true
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

        // iOS source set - explicit dependency setup for proper hierarchy
        val iosMain by creating {
            dependsOn(commonMain.get())
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        jvmMain {
            dependencies {
                // Uses java.awt.Desktop - no additional dependencies
            }
        }

        // macOS uses StoreKit and NSWorkspace
        val macosMain by creating {
            dependsOn(commonMain.get())
        }
        val macosArm64Main by getting { dependsOn(macosMain) }
        val macosX64Main by getting { dependsOn(macosMain) }
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