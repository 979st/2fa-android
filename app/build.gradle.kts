import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        // https://www.oracle.com/java/technologies/java-se-support-roadmap.html
        // Use latest LTS release
        jvmTarget.set(JvmTarget.JVM_21)

        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xbackend-threads=4",
            "-Xjvm-default=all"
        )

        if (project.hasProperty("release")) {
            freeCompilerArgs.addAll(
                "-Xno-param-assertions",
                "-Xno-call-assertions",
                "-Xno-receiver-assertions",
                "-Xir-aggressive"
            )
        }

        progressiveMode.set(true)
    }
}

android {
    namespace = "app.ninesevennine.twofactorauthenticator"
    compileSdk = 36

    flavorDimensions += "store"
    productFlavors {
        create("standard") {
            dimension = "store"
            applicationId = "app.ninesevennine.twofactorauthenticator"
        }
        create("accrescent") {
            dimension = "store"
            applicationId = "app.ninesevennine.twofactorauthenticator.accrescent"
        }
        create("play") {
            dimension = "store"
            applicationId = "app.ninesevennine.twofactorauthenticator.play"
        }
    }

    defaultConfig {
        minSdk = 34
        targetSdk = 36
        versionCode = 27
        versionName = "Beta 18"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            enableV1Signing = false
            enableV2Signing = true // Required for Obtainium
            enableV3Signing = false // Temporarily disable V3 signing
            enableV4Signing = false // Temporarily disable V4 signing
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isProfileable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false

            //noinspection ChromeOsAbiSupport
            ndk { abiFilters += "arm64-v8a" }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-speed.pro"
            )

            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    excludes += "DebugProbesKt.bin"
                    excludes += "kotlin-tooling-metadata.json"

                    pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
                }
                jniLibs {
                    useLegacyPackaging = false
                }
            }
        }
    }

    applicationVariants.all {
        val variant = this
        if (variant.buildType.name == "release") {
            variant.outputs.all {
                val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
                val flavorName = variant.productFlavors.firstOrNull()?.name ?: ""
                output.outputFileName = "twofactorauthenticator-${flavorName}-vc-${variant.versionCode}.apk"
            }
        }
    }

    // https://www.oracle.com/java/technologies/java-se-support-roadmap.html
    // Use latest LTS release
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.splash.screen)
    implementation(libs.reorderable)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.camerax.core)
    implementation(libs.androidx.camerax.camera2)
    implementation(libs.androidx.camerax.lifecycle)
    implementation(libs.androidx.camerax.view)
    implementation(libs.zxing)
    implementation(libs.bouncycastle)
}

tasks.configureEach {
    // Some issue with Java 25 LTS
    if (name.contains("ComposeMapping")) {
        onlyIf { false }
    }
}