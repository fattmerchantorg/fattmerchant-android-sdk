plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
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
        
        // Export ProGuard rules to consuming apps
        consumerProguardFiles("proguard-rules.pro")
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

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.3.0"
    }
}

dependencies {
    // AndroidX AppCompat (Required by Cloud Commerce SDK themes)
    implementation("androidx.appcompat:appcompat:1.7.1")
    
    // Jetpack Compose - For TapToPayPrompt UI
    implementation("androidx.activity:activity-compose:1.12.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    api(platform("androidx.compose:compose-bom:2026.01.01"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.ui:ui-graphics")
    api("androidx.compose.ui:ui-tooling-preview")
    api("androidx.compose.material3:material3")
    api("androidx.compose.runtime:runtime:1.10.2")

    // NMI Cloud Commerce SDK - Tap to Pay Support
    // Using MTF (test) SDK for development - both SDKs contain duplicate classes and cannot be included together
    // Using compileOnly to avoid AAR bundling issues - app module must include this dependency
    compileOnly(files("libs/cloud-commerce-sdk-mtf-5.3.0.aar"))
    // compileOnly(files("libs/cloud-commerce-sdk-5.3.0.aar")) // Use production SDK for release builds
    
    // NMI Legacy Dependencies
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    //noinspection Aligned16KB
    implementation("net.zetetic:android-database-sqlcipher:4.5.4@aar")
    implementation("androidx.sqlite:sqlite:2.6.2")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Retrofit & OkHttp (Required by Cloud Commerce SDK)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    
    // Moshi & Gson (JSON Parsing)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.google.code.gson:gson:2.13.2")
    
    // RxJava (Required by Cloud Commerce SDK)
    implementation("io.reactivex.rxjava3:rxjava:3.1.12")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    
    // Google Play Services - Location & Integrity (Required for Tap to Pay)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.play:integrity:1.6.0")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Ktor
    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-okhttp:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-gson:3.4.0")
}
