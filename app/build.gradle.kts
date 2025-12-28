import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
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
        versionCode = 28
        versionName = "Beta 19"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a"))
        }

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
        release {
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            packaging {
                resources {
                    excludes += listOf(
                        "META-INF/*.kotlin_module",
                        "META-INF/DEPENDENCIES",
                        "META-INF/LICENSE",
                        "META-INF/LICENSE.txt",
                        "META-INF/NOTICE",
                        "META-INF/NOTICE.txt",
                        "META-INF/*.version",
                        "META-INF/INDEX.LIST",
                        "META-INF/io.netty.versions.properties"
                    )
                }
            }

            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }

        debug {
            isDebuggable = true
        }
    }

    applicationVariants.all {
        outputs.forEach { output ->
            if (output is BaseVariantOutputImpl) {
                val variantOutput = output as ApkVariantOutputImpl
                if (buildType.name == "release") {
                    val flavorName = productFlavors.firstOrNull()?.name ?: "standard"
                    variantOutput.outputFileName =
                        "twofactorauthenticator-${flavorName}-vc-${versionCode}.apk"
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)

            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }

    // https://slack-chats.kotlinlang.org/t/32897179/when-compiling-release-builds-with-kotlin-2-3-0-i-get-execut
    composeCompiler {
        includeComposeMappingFile.set(false)
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
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