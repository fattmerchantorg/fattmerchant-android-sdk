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

        // Default values — overridden by the chipdnatest flavor below
        buildConfigField("Boolean", "IS_NMI_TEST_FLAVOR", "false")
        buildConfigField("String", "NMI_TEST_API_KEY", "\"\"")
        buildConfigField("String", "NMI_TEST_APP_ID", "\"\"")
        buildConfigField("String", "NMI_TEST_CERT_FINGERPRINT", "\"\"")
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
        // chipdnatest: uses NMI's pre-registered test identity so we can validate our SDK code path
        // against the same sandbox environment where ChipDnaMobileKotlinDemo succeeds.
        // Falls back to the MTF variant of :cardpresent (sandbox Cloud Commerce SDK).
        create("chipdnatest") {
            dimension = "environment"
            applicationId = "com.creditcall.chipdnamobiledev"
            matchingFallbacks += listOf("mtf")
            buildConfigField("Boolean", "IS_NMI_TEST_FLAVOR", "true")
            buildConfigField("String", "NMI_TEST_API_KEY", "\"7WXb667D6MSj5CcWj85Bf3Jn5F6DqAW5\"")
            buildConfigField("String", "NMI_TEST_APP_ID", "\"STAXDEMO\"")
            buildConfigField("String", "NMI_TEST_CERT_FINGERPRINT", "\"03:F9:1D:8A:E7:8F:E3:8A:0E:48:1F:93:46:84:32:0E:B3:DB:F3:2F:7B:3C:4D:26:EE:82:1F:A0:56:46:21:31\"")
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
    implementation("androidx.activity:activity-compose:1.12.4")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")

    // Google Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
}