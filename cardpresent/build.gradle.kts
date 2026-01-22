plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    signing
}

apply(from = "${rootProject.projectDir}/gradle/publish-module.gradle.kts")

// Set group: JitPack passes -Pgroup, otherwise use default for Maven Central
group = project.findProperty("group") as String? ?: "com.fattmerchant"

android {
    namespace = "com.fattmerchant"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        compose = true
    }
    
    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // AndroidX AppCompat (Required by Cloud Commerce SDK themes)
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Jetpack Compose - For TapToPayPrompt UI
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime:1.10.1")

    // NMI Cloud Commerce SDK - Tap to Pay Support
    // Using MTF (test) SDK for development - both SDKs contain duplicate classes and cannot be included together
    // Using compileOnly to avoid AAR bundling issues - app module must include this dependency
    compileOnly(files("libs/cloud-commerce-sdk-mtf-5.3.0.aar"))
    // compileOnly(files("libs/cloud-commerce-sdk-5.3.0.aar")) // Use production SDK for release builds
    
    // NMI Legacy Dependencies
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("net.zetetic:android-database-sqlcipher:4.5.0@aar")
    implementation("androidx.sqlite:sqlite:2.2.0")
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Retrofit & OkHttp (Required by Cloud Commerce SDK)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Moshi & Gson (JSON Parsing)
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // RxJava (Required by Cloud Commerce SDK)
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    
    // Google Play Services - Location & Integrity (Required for Tap to Pay)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.play:integrity:1.3.0")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Ktor
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-okhttp:2.3.6")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-gson:2.3.6")
}
