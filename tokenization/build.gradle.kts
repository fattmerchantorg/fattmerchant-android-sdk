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
    compileSdk = 34
    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
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
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.okio:okio:3.6.0")
}
