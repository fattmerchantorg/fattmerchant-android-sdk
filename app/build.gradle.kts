import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.android.utils.jvmArchitecture

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val staxApiKey: String = gradleLocalProperties(rootDir, providers).getProperty("staxApiKey") ?: "NoApiKey"

android {
    namespace = "com.staxpayments.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.staxpayments.sample"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    packaging {
        resources {
            excludes += "META-INF/*.kotlin_module"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Define product flavors to match cardpresent module
    flavorDimensions += "environment"
    productFlavors {
        create("production") {
            dimension = "environment"
        }
        create("mtf") {
            dimension = "environment"
            applicationIdSuffix = ".mtf"
        }
    }

    buildTypes {
        release {
            buildConfigField("String", "STAX_API_KEY", "\"$staxApiKey\"")
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
        debug {
            isDefault = true
            isDebuggable = true
            buildConfigField("String", "STAX_API_KEY", "\"$staxApiKey\"")
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Stax SDK (includes Cloud Commerce SDK transitively)
    implementation(project(":cardpresent"))

    // Dependencies
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.3")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2026.01.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")

    // Google Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
}