import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val staxApiKey: String = gradleLocalProperties(rootDir, providers).getProperty("staxApiKey") ?: "NoApiKey"

android {
    namespace = "com.staxpayments"
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
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // NMI Cloud Commerce SDK - Required for Tap to Pay (matches cardpresent module)
    // Using MTF (test) version for development
    implementation(files("../cardpresent/libs/cloud-commerce-sdk-mtf-5.3.0.aar"))
    
    // Stax SDK
    implementation(project(":cardpresent"))

    // Dependencies
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.1")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Google Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
}